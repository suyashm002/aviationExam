#!/usr/bin/env python3
"""
PDF Parser for Civil Aviation Mock Exam Questions

This script helps extract questions from PDF files and convert them to JSON format
for import into the Android app.

Requirements:
    pip install PyPDF2 pdfplumber pytesseract pillow pdf2image

For scanned PDFs (OCR):
    pip install pytesseract
    Install Tesseract OCR: https://github.com/tesseract-ocr/tesseract
"""

import json
import re
import os
from pathlib import Path
from typing import List, Dict, Optional
import argparse

try:
    import pdfplumber
    PDFPLUMBER_AVAILABLE = True
except ImportError:
    PDFPLUMBER_AVAILABLE = False
    print("Warning: pdfplumber not installed. Install with: pip install pdfplumber")

try:
    from pdf2image import convert_from_path
    import pytesseract
    OCR_AVAILABLE = True
except ImportError:
    OCR_AVAILABLE = False
    print("Warning: OCR libraries not installed. Install with: pip install pytesseract pdf2image pillow")

# Section mapping - adjust based on your PDF structure
SECTION_KEYWORDS = {
    "air_law": ["air law", "airlaw", "regulations", "legal", "icao annex"],
    "meteorology": ["meteorology", "weather", "met", "atmospheric"],
    "principles_of_flight": ["principles of flight", "aerodynamics", "flight mechanics", "pof"],
    "aircraft_general": ["aircraft general", "aircraft systems", "aircraft knowledge", "agk"],
    "human_performance": ["human performance", "human factors", "human limitations", "hpl"],
    "operational_procedures": ["operational procedures", "ops", "procedures", "sop"],
    "navigation": ["navigation", "nav", "vor", "ndb", "gps", "charts"],
    "communication": ["communication", "radio", "phraseology", "rt", "comms"]
}

def detect_section(text: str) -> Optional[str]:
    """Detect section based on text content"""
    text_lower = text.lower()
    for section_id, keywords in SECTION_KEYWORDS.items():
        for keyword in keywords:
            if keyword in text_lower:
                return section_id
    return None

def extract_text_from_pdf(pdf_path: str, use_ocr: bool = False) -> str:
    """Extract text from PDF file"""
    if use_ocr and OCR_AVAILABLE:
        print(f"Using OCR for {pdf_path}...")
        try:
            images = convert_from_path(pdf_path)
            text = ""
            for image in images:
                text += pytesseract.image_to_string(image) + "\n"
            return text
        except Exception as e:
            print(f"OCR failed: {e}. Falling back to text extraction...")
    
    if PDFPLUMBER_AVAILABLE:
        try:
            with pdfplumber.open(pdf_path) as pdf:
                text = ""
                for page in pdf.pages:
                    text += page.extract_text() or ""
                return text
        except Exception as e:
            print(f"Error extracting text: {e}")
            return ""
    else:
        print("Error: No PDF parsing library available. Install pdfplumber or use OCR.")
        return ""

def parse_question_block(text: str, question_num: int) -> Optional[Dict]:
    """
    Parse a question block from text.
    Adjust this function based on your PDF format.
    """
    # Common patterns for questions
    patterns = [
        # Pattern 1: "Q1. Question text?\nA) Option\nB) Option\n..."
        r'Q\d+[\.\)]\s*(.+?)\n([A-D][\.\)]\s*.+?)\n([A-D][\.\)]\s*.+?)\n([A-D][\.\)]\s*.+?)\n([A-D][\.\)]\s*.+?)(?:\n.*?Answer[:\s]+([A-D]))?',
        # Pattern 2: "1. Question text\nA. Option\n..."
        r'\d+[\.\)]\s*(.+?)\n([A-D][\.\)]\s*.+?)\n([A-D][\.\)]\s*.+?)\n([A-D][\.\)]\s*.+?)\n([A-D][\.\)]\s*.+?)(?:\n.*?[Cc]orrect[:\s]+([A-D]))?',
    ]
    
    for pattern in patterns:
        matches = re.finditer(pattern, text, re.DOTALL | re.IGNORECASE)
        for match in matches:
            question_text = match.group(1).strip()
            options = [
                match.group(2).strip().replace("A)", "").replace("A.", "").strip(),
                match.group(3).strip().replace("B)", "").replace("B.", "").strip(),
                match.group(4).strip().replace("C)", "").replace("C.", "").strip(),
                match.group(5).strip().replace("D)", "").replace("D.", "").strip(),
            ]
            
            # Try to find correct answer
            correct_answer = None
            if match.lastindex >= 6 and match.group(6):
                answer_letter = match.group(6).upper()
                correct_answer = ord(answer_letter) - ord('A')
            else:
                # Look for answer in surrounding text
                answer_match = re.search(r'[Aa]nswer[:\s]+([A-D])', text[match.end():match.end()+200])
                if answer_match:
                    correct_answer = ord(answer_match.group(1).upper()) - ord('A')
            
            if correct_answer is None:
                correct_answer = 0  # Default to first option if not found
            
            return {
                "questionText": question_text,
                "options": options,
                "correctAnswer": correct_answer,
                "explanation": "",  # Will need manual review
                "difficulty": "medium"
            }
    
    return None

def process_pdf_file(pdf_path: str, output_dir: str, use_ocr: bool = False) -> Dict[str, List[Dict]]:
    """Process a single PDF file and extract questions"""
    print(f"\nProcessing: {pdf_path}")
    text = extract_text_from_pdf(pdf_path, use_ocr)
    
    if not text:
        print(f"  Warning: Could not extract text from {pdf_path}")
        return {}
    
    # Detect section
    section = detect_section(text)
    if not section:
        print(f"  Warning: Could not detect section. Using 'air_law' as default.")
        section = "air_law"
    
    # Split text into potential question blocks
    # Adjust splitting logic based on your PDF format
    question_blocks = re.split(r'(?:Q\d+|Question\s+\d+|\d+[\.\)]\s*(?=[A-Z]))', text, flags=re.IGNORECASE)
    
    questions = []
    for i, block in enumerate(question_blocks[1:], 1):  # Skip first empty split
        question = parse_question_block(block, i)
        if question:
            question_id = f"{section}_{len(questions) + 1:03d}"
            question["id"] = question_id
            question["section"] = section.replace("_", " ").title()
            question["references"] = []
            questions.append(question)
    
    print(f"  Extracted {len(questions)} questions for section: {section}")
    
    return {section: questions}

def save_questions_to_json(questions_by_section: Dict[str, List[Dict]], output_dir: str):
    """Save questions to JSON files organized by section"""
    os.makedirs(output_dir, exist_ok=True)
    
    for section, questions in questions_by_section.items():
        if questions:
            output_file = os.path.join(output_dir, f"questions_{section}.json")
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(questions, f, indent=2, ensure_ascii=False)
            print(f"  Saved {len(questions)} questions to {output_file}")

def merge_questions_from_directory(pdf_directory: str, output_dir: str, use_ocr: bool = False):
    """Process all PDFs in a directory"""
    pdf_dir = Path(pdf_directory)
    all_questions = {}
    
    pdf_files = list(pdf_dir.glob("*.pdf")) + list(pdf_dir.glob("*.PDF"))
    
    if not pdf_files:
        print(f"No PDF files found in {pdf_directory}")
        return
    
    print(f"Found {len(pdf_files)} PDF files")
    
    for pdf_file in pdf_files:
        try:
            questions = process_pdf_file(str(pdf_file), output_dir, use_ocr)
            for section, q_list in questions.items():
                if section not in all_questions:
                    all_questions[section] = []
                all_questions[section].extend(q_list)
        except Exception as e:
            print(f"  Error processing {pdf_file}: {e}")
    
    # Save merged questions
    save_questions_to_json(all_questions, output_dir)
    
    # Print summary
    print("\n" + "="*50)
    print("SUMMARY")
    print("="*50)
    total = 0
    for section, questions in all_questions.items():
        print(f"{section}: {len(questions)} questions")
        total += len(questions)
    print(f"Total: {total} questions")
    print(f"\nOutput directory: {output_dir}")

def main():
    parser = argparse.ArgumentParser(description='Parse PDF files and extract exam questions')
    parser.add_argument('pdf_directory', help='Directory containing PDF files')
    parser.add_argument('-o', '--output', default='output', help='Output directory for JSON files')
    parser.add_argument('--ocr', action='store_true', help='Use OCR for scanned PDFs')
    parser.add_argument('--single', help='Process a single PDF file instead of directory')
    
    args = parser.parse_args()
    
    if args.single:
        # Process single file
        questions = process_pdf_file(args.single, args.output, args.ocr)
        save_questions_to_json(questions, args.output)
    else:
        # Process directory
        merge_questions_from_directory(args.pdf_directory, args.output, args.ocr)

if __name__ == "__main__":
    main()

