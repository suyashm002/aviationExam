# PDF Parsing Guide - How to Extract Questions from Your PDFs

## Overview

You have hundreds of PDF files with mock exam questions. This guide will help you extract them and import them into the app.

## Option 1: Automated Parsing (Recommended)

### Step 1: Install Python and Dependencies

```bash
# Install Python 3.8+ if not already installed
# Then install dependencies:
cd pdf_parser
pip install -r requirements.txt

# For scanned PDFs, install Tesseract OCR:
# macOS: brew install tesseract
# Windows: Download from https://github.com/UB-Mannheim/tesseract/wiki
# Linux: sudo apt-get install tesseract-ocr
```

### Step 2: Run the Parser

```bash
# Process all PDFs in a directory
python pdf_parser.py /path/to/your/pdf/folder -o output

# Use OCR for scanned PDFs
python pdf_parser.py /path/to/your/pdf/folder -o output --ocr

# Process a single PDF to test
python pdf_parser.py /path/to/your/pdf/folder -o output --single test.pdf
```

### Step 3: Review and Fix

1. Check the output JSON files in the `output/` directory
2. Review questions for accuracy
3. Add explanations where missing
4. Verify correct answers (0-3 index)
5. Add references

### Step 4: Import into App

1. Create directory: `app/src/main/assets/questions/`
2. Copy JSON files there:
   - `questions_air_law.json`
   - `questions_meteorology.json`
   - etc.
3. Rebuild the app - questions will load automatically!

## Option 2: Manual Extraction (If Automated Fails)

### Step 1: Extract Text from PDFs

1. Open PDF in a text editor or PDF viewer
2. Copy question text
3. Format according to template

### Step 2: Use JSON Template

Use `pdf_parser/manual_extraction_template.json` as a template:

```json
[
  {
    "id": "air_law_001",
    "section": "Air Law",
    "questionText": "Your question here?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctAnswer": 0,
    "explanation": "Explanation here",
    "difficulty": "medium",
    "references": ["Reference 1", "Reference 2"]
    }
]
```

### Step 3: Create JSON Files

1. Create one JSON file per section
2. Name them: `questions_{section_id}.json`
3. Place in `app/src/main/assets/questions/`

## Option 3: Share PDFs for Processing

If you want me to help process them:

1. **Upload a few sample PDFs** (2-3 files) so I can:
   - Analyze the format
   - Customize the parser
   - Provide specific instructions

2. **Share the PDF structure**:
   - How are questions formatted?
   - How are options labeled? (A/B/C/D or 1/2/3/4)
   - Where are correct answers indicated?
   - Are there section headers?

3. **Batch process**:
   - Once format is confirmed, process all PDFs
   - Review and fix any errors
   - Import into app

## Quick Start - Minimal Setup

If you want to start quickly:

1. **Pick 5-10 PDFs** from different sections
2. **Run the parser** on just those:
   ```bash
   python pdf_parser.py /path/to/sample/pdfs -o test_output
   ```
3. **Check the output** - verify questions look correct
4. **Adjust parser** if needed (edit `pdf_parser.py`)
5. **Process all PDFs** once format is confirmed

## Common PDF Formats

### Format 1: Numbered Questions
```
1. What is the minimum visibility for VFR flight?
A) 1 km
B) 5 km
C) 8 km
D) 10 km
Answer: C
```

### Format 2: Q Prefix
```
Q1. What is the minimum visibility for VFR flight?
A) 1 km
B) 5 km
C) 8 km
D) 10 km
Correct Answer: C
```

### Format 3: Lettered Options
```
Question: What is the minimum visibility for VFR flight?
(a) 1 km
(b) 5 km
(c) 8 km
(d) 10 km
Answer: (c)
```

The parser handles common formats, but you may need to customize it for your specific PDF structure.

## Tips for Best Results

1. **Start Small**: Test with 5-10 PDFs first
2. **Check Format**: Review a few PDFs manually to understand structure
3. **Use OCR Sparingly**: Only for scanned PDFs (slower, less accurate)
4. **Review Output**: Always review extracted questions
5. **Batch Process**: Once format is confirmed, process all at once

## Troubleshooting

### Parser Not Extracting Questions
- Check PDF format matches parser patterns
- Try OCR mode for scanned PDFs
- Manually review a PDF to understand structure
- Customize `parse_question_block()` function

### Wrong Section Detection
- Add keywords to `SECTION_KEYWORDS` in parser
- Manually categorize if needed
- Check section headers in PDFs

### OCR Not Working
- Ensure Tesseract is installed
- Check image quality
- Try preprocessing images

## Next Steps After Parsing

1. ✅ Review all JSON files
2. ✅ Fix any errors
3. ✅ Add missing explanations
4. ✅ Verify correct answers
5. ✅ Copy to `app/src/main/assets/questions/`
6. ✅ Rebuild app
7. ✅ Test with real questions!

## Need Help?

If you encounter issues:
1. Share a sample PDF (or screenshot of format)
2. Share the error messages
3. I can help customize the parser for your specific format

