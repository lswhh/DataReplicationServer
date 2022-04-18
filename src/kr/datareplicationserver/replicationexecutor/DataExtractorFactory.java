package kr.datareplicationserver.replicationexecutor;

public interface DataExtractorFactory {

	public DataExtractor makeDataExtractor(DataExtractorType type); 
}
