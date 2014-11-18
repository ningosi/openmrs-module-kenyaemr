package org.openmrs.module.kenyaemr.fragment.controller.summaries;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;

/**
 * Controller.
 */
public class DownLoadFragmentController {

	public void downloadPDF() throws IOException, COSVisitorException {
		PDDocument document = new PDDocument();
		// Create a new blank page and add it to the document
		PDPage blankPage = new PDPage();
		document.addPage(blankPage);

		// Save the newly created document
		document.save("BlankPage.pdf");

		// finally make sure that the document is properly
		// closed.
		document.close();
	}
}
