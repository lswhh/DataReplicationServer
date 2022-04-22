package kr.datareplicationserver.replicationexecutor;

import kr.datareplicationserver.dataextractor.DataExtractorFactoryImpl;

public class Syncer extends Executor {

	public Syncer() {
		// TODO Auto-generated constructor stub
	}
	
	public void SyncTable( /* */ ) { // get configure
		//table sync
		DataExtractorFactory dataExtractorFactory = new DataExtractorFactoryImpl();
		DataExtractor syncDataExtractor = dataExtractorFactory.makeDataExtractor(DataExtractorType.SYNC_DATA);
	}
}
