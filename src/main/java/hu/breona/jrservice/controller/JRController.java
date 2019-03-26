package hu.breona.jrservice.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import hu.breona.jrservice.service.JRService;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

@Controller
public class JRController {
	
	@Autowired
	private JRService jrService;
	
	@RequestMapping(path = "/pdf", method = RequestMethod.GET)
	public void report(@RequestParam String report, ModelAndView model, HttpServletRequest request, HttpServletResponse response) 
			throws IOException, JRException, SQLException {

		JasperPrint jasperPrint = null;
		
		OutputStream out = response.getOutputStream();
		response.setContentType("application/x-download");
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"report.pdf\""));

		Map<String, Object> params = ((Map<String,String[]>)request.getParameterMap())
				.entrySet().stream()
				.filter(e -> !"report".equalsIgnoreCase(e.getKey()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()[0]));
		
		jasperPrint = jrService.compileAndFillReport(report, params);
		JasperExportManager.exportReportToPdfStream(jasperPrint, out);
	}

	
}
