package org.openmrs.module.kenyaemr.reporting.renderer;

import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.ReportRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by codehub on 23/05/14.
 */
public class DhisReportRenderer implements ReportRenderer {
	@Override
	public boolean canRender(ReportDefinition reportDefinition) {
		return true;
	}

	@Override
	public Collection<RenderingMode> getRenderingModes(ReportDefinition reportDefinition) {
		return Collections.singleton(new RenderingMode());
	}

	@Override
	public String getRenderedContentType(ReportDefinition reportDefinition, String s) {
		return "text/xml";
	}

	@Override
	public String getFilename(ReportDefinition reportDefinition, String s) {
		return "test.xml";
	}

	@Override
	public void render(ReportData reportData, String s, OutputStream outputStream) throws IOException, RenderingException {

	}
}
