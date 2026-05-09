# PDF Parser for Civil Aviation Mock Exam Questions

This tool helps you extract questions from your PDF files and convert them to JSON format for the Android app.

## Quick Start

### 1. Install Dependencies

```bash
# Install Python dependencies
pip install -r requirements.txt

# For scanned PDFs, also install Tesseract OCR:
# Windows: Download from https://github.com/UB-Mannheim/tesseract/wiki
# macOS: brew install tesseract
# Linux: sudo apt-get install tesseract-ocr
```

### 2. Basic Usage

```bash
# Process all PDFs in a directory
python pdf_parser.py /path/to/your/pdfs -o output

# Process a single PDF file
python pdf_parser.py /path/to/your/pdfs -o output --single file.pdf

# Use OCR for scanned PDFs
python pdf_parser.py /path/to/your/pdfs -o output --ocr
```

### 3. Output

The script will create JSON files in the output directory:
- `questions_air_law.json`
- `questions_meteorology.json`
- `questions_principles_of_flight.json`
- etc.

## Manual Review Process

After parsing, you'll need to:

1. **Review extracted questions** - Check for accuracy
2. **Add explanations** - Fill in the explanation field
3. **Verify correct answers** - Ensure correctAnswer index is correct (0-3)
4. **Add references** - Add relevant book/chapter references
5. **Categorize properly** - Move questions to correct sections if needed

## JSON Format

Each question should follow this format:

```json
{
  "id": "air_law_001",
  "section": "Air Law",
  "questionText": "What is the minimum visibility required for VFR flight?",
  "options": ["1 km", "5 km", "8 km", "10 km"],
  "correctAnswer": 2,
  "explanation": "According to ICAO Annex 2, the minimum visibility for VFR flight in controlled airspace is 8 km.",
  "difficulty": "medium",
  "references": ["ICAO Annex 2", "EASA Part-FCL"]
}
```

## Customization

### Adjust Section Detection

Edit `SECTION_KEYWORDS` in `pdf_parser.py` to match your PDF structure:

```python
SECTION_KEYWORDS = {
    "air_law": ["air law", "airlaw", "your keywords here"],
    # ... add more keywords
}
```

### Adjust Question Parsing

Modify the `parse_question_block()` function to match your PDF format. Common formats:

- **Format 1**: Q1. Question?\nA) Option A\nB) Option B\n...
- **Format 2**: 1. Question\nA. Option A\nB. Option B\n...
- **Format 3**: Question text\n(a) Option A\n(b) Option B\n...

## Troubleshooting

### OCR Not Working
- Ensure Tesseract is installed and in PATH
- Check image quality - OCR works best with clear, high-resolution scans
- Try preprocessing images (increase contrast, remove noise)

### Questions Not Extracted
- Check the PDF format - the parser may need customization
- Try OCR mode if PDF is scanned
- Manually review a few PDFs to understand the format

### Wrong Section Detection
- Add more keywords to `SECTION_KEYWORDS`
- Check if section headers are consistent in your PDFs
- Manually categorize if needed

## Alternative: Manual Extraction

If automated parsing doesn't work well, you can:

1. **Use a PDF editor** to extract text
2. **Copy questions manually** into a text file
3. **Use the JSON template** to format questions
4. **Import into the app** using the database initializer

## Next Steps

After parsing:

1. Review all JSON files
2. Fix any errors in questions
3. Add explanations and references
4. Copy JSON files to `app/src/main/assets/questions/` in your Android project
5. Update `DatabaseInitializer.kt` to load from JSON files

## Batch Processing Tips

1. **Process in batches** - Don't process all 100+ PDFs at once initially
2. **Test with 5-10 PDFs first** - Verify the output format
3. **Review sample output** - Check if questions are extracted correctly
4. **Adjust parser** - Customize based on your PDF format
5. **Process remaining PDFs** - Once format is verified

## Support

If you encounter issues:
1. Check the error messages
2. Review a sample PDF manually
3. Adjust the parser patterns
4. Consider manual extraction for complex formats

