package com.vvt.prot.databuilder;

import java.io.IOException;

import com.vvt.prot.CommandCode;
import com.vvt.prot.CommandData;
import com.vvt.prot.DataProvider;
import com.vvt.prot.CommandMetaData;
import com.vvt.prot.CommandRequest;

public class GetAddrBookDataBuilder {


	
	
	
	//Override
	public CommandCode parseCmdCode() {
		return CommandCode.GET_ADDRESS_BOOK;
	}

	

	public void buildPayload(CommandData cmdData, CommandMetaData cmdMetaData) {
		// TODO Auto-generated method stub
		
	}



	//@Override
	public PayloadBuilderResponse buildPayload(CommandRequest cmdRequest) throws IOException,
			InterruptedException {
				return null;
		// TODO Auto-generated method stub
		
	}
}
