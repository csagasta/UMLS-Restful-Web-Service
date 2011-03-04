package org.ohd.umls.restservice;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.ohd.umls.UMLSInterface;

@Path("umls")
public class UmlsService {
	private UMLSInterface umls = null;
	private Properties conf;
	
	public UmlsService(){
		conf = new Properties();
		try {
			conf.load(this.getClass().getClassLoader().getResourceAsStream("/config.properties"));
			umls = new UMLSInterface(conf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Path("/html/cuis")
	@GET
	@Produces("text/html")
	public String findAsHTML(@QueryParam("term") String term){
		TermResult tr = findCuisByTerm(term);
		StringBuffer resp = new StringBuffer();
		resp.append("<h2>Submitted Term: ");
		resp.append(term);
		resp.append("</h2>");
		resp.append("<p>Normalized to ");
		resp.append(tr.norm);
		resp.append("</p>");
		resp.append("<h3>Unique Concept List</h3>");
		resp.append("<ul>");
		for (String c : tr.cuis){
			resp.append("<li>");
			String pt = umls.getPreferredTerm(c);
			resp.append(pt);
			resp.append(" [");
			resp.append(c);
			resp.append("]");
			resp.append("</li>");
		}
		resp.append("</ul>");
		return resp.toString();
	}
	
	@Path("/xml/cuis")
	@GET
	@Produces("application/xml")
	public String findAsXML(@QueryParam("term") String term){
		TermResult tr = findCuisByTerm(term);
		StringBuffer resp = new StringBuffer();
		resp.append("<ResultSet term='");
		resp.append(term);
		resp.append("'>");
		resp.append("<NormalizedString>");
		resp.append(tr.norm);
		resp.append("</NormalizedString>");
		for (String c : tr.cuis){
			resp.append("<CUI>");
			resp.append(c);
			resp.append("</CUI>");
		}
		resp.append("</ResultSet>");
		return resp.toString();
	}
	
	private TermResult findCuisByTerm(String term){
		String norm = umls.normalize(term);
		return new TermResult(term, norm, umls.getCUIs(norm));
	}
	
	protected class TermResult{
		String term;
		String norm;
		List<String> cuis;
		protected TermResult(String term, String norm, List<String> cuis) {
			super();
			this.term = term;
			this.norm = norm;
			this.cuis = cuis;
		}
		
	}
}
