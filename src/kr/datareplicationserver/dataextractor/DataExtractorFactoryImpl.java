package kr.datareplicationserver.dataextractor;

import kr.datareplicationserver.replicationexecutor.DataExtractor;
import kr.datareplicationserver.replicationexecutor.DataExtractorFactory;
import kr.datareplicationserver.replicationexecutor.DataExtractorType;

public class DataExtractorFactoryImpl implements DataExtractorFactory {

	@Override
	public DataExtractor makeDataExtractor(DataExtractorType type) {

		DataExtractor result = null; 
		
		switch (type)
		{
			case SYNC_DATA:
				result = new SyncDataExtractor();
				break;
			case CHANGE_DATA:
				result = new ChangeDataExtractor();
				break;
		}
		return result;
	}
}
