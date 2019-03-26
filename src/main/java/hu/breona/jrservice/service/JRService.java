package hu.breona.jrservice.service;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.crosstabs.JRCrosstab;
import net.sf.jasperreports.engine.JRBreak;
import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRComponentElement;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JREllipse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRFrame;
import net.sf.jasperreports.engine.JRGenericElement;
import net.sf.jasperreports.engine.JRImage;
import net.sf.jasperreports.engine.JRLine;
import net.sf.jasperreports.engine.JRRectangle;
import net.sf.jasperreports.engine.JRStaticText;
import net.sf.jasperreports.engine.JRSubreport;
import net.sf.jasperreports.engine.JRTextField;
import net.sf.jasperreports.engine.JRVisitor;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRElementsVisitor;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

@Service
public class JRService {
	
	@Value("${jrs.report.path}")
	private String reportPath;
	private ArrayList<String>   completedSubReports = new ArrayList<String>(30);
	private Throwable           subReportException  = null;
	
	Logger logger = LogManager.getLogger(JRService.class);

//	@Autowired
//	private ResourceLoader resourceLoader;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	public JasperPrint compileAndFillReport(String report, Map<String, Object> parameters) throws SQLException, IOException, JRException {
  	  Connection conn = jdbcTemplate.getDataSource().getConnection();

//  	  String path = resourceLoader.getResource(String.format("classpath:%s.jrxml", report)).getURI().getPath();

  	  JasperReport jasperReport = compileReport(report);

  	  JasperPrint print = JasperFillManager.fillReport(jasperReport, parameters, conn);
  	  return print; 

	}
	
	public JasperReport compileReport(String reportName) throws JRException {
		JasperDesign jasperDesign = JRXmlLoader.load(reportPath + reportName + ".jrxml");
		  JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
		  JRSaver.saveObject(jasperReport, reportPath + reportName + ".jasper");
		  logger.debug("[+] Saving compiled report to: " + reportPath + reportName + ".jasper");
		  //Compile sub reports
		  JRElementsVisitor.visitReport(jasperReport, new JRVisitor() {
			@Override
		    public void visitBreak(JRBreak breakElement){}

		    @Override
		    public void visitChart(JRChart chart){}

		    @Override
		    public void visitCrosstab(JRCrosstab crosstab){}

		    @Override
		    public void visitElementGroup(JRElementGroup elementGroup){}

		    @Override
		    public void visitEllipse(JREllipse ellipse){}

		    @Override
		    public void visitFrame(JRFrame frame){}

		    @Override
		    public void visitImage(JRImage image){}

		    @Override
		    public void visitLine(JRLine line){}

		    @Override
		    public void visitRectangle(JRRectangle rectangle){}

		    @Override
		    public void visitStaticText(JRStaticText staticText){}

		    @Override
		    public void visitSubreport(JRSubreport subreport){
		      try{
		        String expression = subreport.getExpression().getText().replace(".jasper", "");
		        StringTokenizer st = new StringTokenizer(expression, "\"/");
		        String subReportName = null;
		        while(st.hasMoreTokens())
		          subReportName = st.nextToken();
		        //Sometimes the same subreport can be used multiple times, but
		        //there is no need to compile multiple times
		        if(completedSubReports.contains(subReportName)) return;
		        completedSubReports.add(subReportName);
		        compileReport(subReportName);
		      }
		      catch(Throwable e){
		        subReportException = e;
		      }
		    }
		    @Override
		    public void visitTextField(JRTextField textField){}

		    @Override
		    public void visitComponentElement(JRComponentElement componentElement){}

		    @Override
		    public void visitGenericElement(JRGenericElement element){}
		  });
		  if(subReportException != null) throw new RuntimeException(subReportException);
		  return jasperReport;
		}
}
