package dk.aau.cs.extbi.PFQA.queryOptimizationStrategy;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import dk.aau.cs.extbi.PFQA.helper.Config;
import dk.aau.cs.extbi.PFQA.helper.ContextSet;

public class FullMaterilization extends QueryOptimizationStrategy {
	private String modelName = Config.getNamespace()+"fullMaterilized";

	public FullMaterilization(ContextSet contextSetMinumum) {
		super(contextSetMinumum);
		
		String queryString = createQuery();
		Dataset dataset = TDBFactory.createDataset(Config.getDatasetLocation()) ;
		dataset.begin(ReadWrite.WRITE) ;
			
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, dataset) ;
		Model resultModel = qexec.execConstruct() ;
		qexec.close() ;
			
		dataset.addNamedModel(modelName, resultModel);
		//System.out.println("writing " + resultModel.size() + " triples to "+ Config.getNamespace()+"fullMaterilized");
		
		dataset.commit();
		dataset.end();
	}
	
	private String createQuery() {
		String query = "" +
			"CONSTRUCT { " +
			" ?s ?p ?o . "+
			"} ";
		
		for (String URI : contextSet.getValues()) {
			query += "FROM <"+URI+"> ";
		}
		query += 
			"FROM <"+Config.getCubeInstanceGraphName()+"> " +
			"FROM <"+Config.getCubeStructureGraphName()+"> " +
			"WHERE { "+
			"?s ?p ?o"+
			"}";
		return query;
	}
	
	@Override
	public ResultSet execute(Query aq) {
		Dataset dataset = TDBFactory.createDataset(Config.getDatasetLocation()) ;
		dataset.begin(ReadWrite.READ) ;
		Model model = dataset.getNamedModel(modelName);
		QueryExecution qexec = QueryExecutionFactory.create(aq, model) ;
		ResultSet results = qexec.execSelect() ;
		
		dataset.commit();
		dataset.end();
		return results;
	}
}
