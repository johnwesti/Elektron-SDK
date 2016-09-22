///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2016. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.examples.training.iprovider.series200.example250__GroupStatus__Fanout;

/*
assumptions:
- dependence on configuration from the EmaConfig.xml file
- server's port from the configuration file.
- source directory refresh message from the configuration file
- standard RDM file dictionary located in the same directory as the app
- directory requests processed by the EMA
- dictionary requests processed by the EMA

this interactive provider shows the following functionality
- accepting login requests in the OMM way
- responding to the marketprice streaming requests
- after submitting refresh and 10 updates,
  sending a directory update with group status (using the domain Directory helper class)
- all other domains are status closed
*/

import java.nio.ByteBuffer;

import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.FieldList;
import com.thomsonreuters.ema.access.GenericMsg;
import com.thomsonreuters.ema.access.Msg;
import com.thomsonreuters.ema.access.OmmException;
import com.thomsonreuters.ema.access.OmmIProviderConfig;
import com.thomsonreuters.ema.access.OmmProvider;
import com.thomsonreuters.ema.access.OmmProviderClient;
import com.thomsonreuters.ema.access.OmmProviderEvent;
import com.thomsonreuters.ema.access.OmmReal;
import com.thomsonreuters.ema.access.OmmState;
import com.thomsonreuters.ema.access.PostMsg;
import com.thomsonreuters.ema.access.RefreshMsg;
import com.thomsonreuters.ema.access.ReqMsg;
import com.thomsonreuters.ema.access.StatusMsg;
import com.thomsonreuters.ema.domain.DomainFactory;
import com.thomsonreuters.ema.domain.directory.Dir;
import com.thomsonreuters.ema.rdm.EmaRdm;

class AppClient implements OmmProviderClient
{
	public long itemHandle = 0;
	public ByteBuffer groupId = ByteBuffer.wrap("1".getBytes());

	public void onReqMsg(ReqMsg reqMsg, OmmProviderEvent event)
	{
		switch (reqMsg.domainType())
		{
			case EmaRdm.MMT_LOGIN :
				processLoginRequest(reqMsg, event);
				break;
			case EmaRdm.MMT_MARKET_PRICE :
				processMarketPriceRequest(reqMsg, event);
				break;
			default :
				processInvalidItemRequest(reqMsg, event);
				break;
		}
	}
	
	public void onRefreshMsg(RefreshMsg refreshMsg,	OmmProviderEvent event){}
	public void onStatusMsg(StatusMsg statusMsg, OmmProviderEvent event){}
	public void onGenericMsg(GenericMsg genericMsg, OmmProviderEvent event){}
	public void onPostMsg(PostMsg postMsg, OmmProviderEvent event){}
	public void onReissue(ReqMsg reqMsg, OmmProviderEvent event){}
	public void onClose(ReqMsg reqMsg, OmmProviderEvent event){}
	public void onAllMsg(Msg msg, OmmProviderEvent event){}
	
	void processLoginRequest(ReqMsg reqMsg, OmmProviderEvent event)
	{
		event.provider().submit( EmaFactory.createRefreshMsg().domainType(EmaRdm.MMT_LOGIN).name(reqMsg.name()).nameType(EmaRdm.USER_NAME)
				.complete(true).solicited(true).state(OmmState.StreamState.OPEN, OmmState.DataState.OK, OmmState.StatusCode.NONE, "Login accepted"),
				event.handle() );
	}
	
	void processUpdateGroupStatus( OmmProvider provider)
	{
		Dir.Info info = DomainFactory.Directory.createInfo();
		info.serviceId(1).serviceName("DIRECT_FEED");
		
		Dir.Group group = DomainFactory.Directory.createGroup();
		group.groupId( groupId );
		group.status( OmmState.StreamState.CLOSED, OmmState.DataState.SUSPECT, OmmState.StatusCode.NONE, "" );

		Dir.Service service = DomainFactory.Directory.createService();
		service.info( info ).addGroup( group );
		
		Dir.Directory directory = DomainFactory.Directory.createDirectory();
		directory.addService( service );
		
		provider.submit( EmaFactory.createUpdateMsg().domainType(EmaRdm.MMT_DIRECTORY)
				.payload(directory.data()), 0 ); // use 0 item handle to fanout to all subscribers
	}
	
	void processMarketPriceRequest(ReqMsg reqMsg, OmmProviderEvent event)
	{
		if(itemHandle != 0)
		{
			processInvalidItemRequest(reqMsg, event);
			return;
		}

		FieldList fieldList = EmaFactory.createFieldList();
		fieldList.add( EmaFactory.createFieldEntry().real(22, 3990, OmmReal.MagnitudeType.EXPONENT_NEG_2));
		fieldList.add( EmaFactory.createFieldEntry().real(25, 3994, OmmReal.MagnitudeType.EXPONENT_NEG_2));
		fieldList.add( EmaFactory.createFieldEntry().real(30, 9,  OmmReal.MagnitudeType.EXPONENT_0));
		fieldList.add( EmaFactory.createFieldEntry().real(31, 19, OmmReal.MagnitudeType.EXPONENT_0));
		
		event.provider().submit( EmaFactory.createRefreshMsg().itemGroup(groupId)
				.name(reqMsg.name()).serviceName(reqMsg.serviceName()).solicited(true)
				.state(OmmState.StreamState.OPEN, OmmState.DataState.OK, OmmState.StatusCode.NONE, "Refresh Completed")
				.payload(fieldList).complete(true),
				event.handle() );
		
		itemHandle = event.handle();
	}
	
	void processInvalidItemRequest(ReqMsg reqMsg, OmmProviderEvent event)
	{
		event.provider().submit( EmaFactory.createStatusMsg().name(reqMsg.name()).serviceName(reqMsg.serviceName())
				.state(OmmState.StreamState.CLOSED, OmmState.DataState.SUSPECT,	OmmState.StatusCode.NOT_FOUND, "Item not found"),
				event.handle() );
	}
}

public class IProvider
{
	public static void main(String[] args)
	{
		OmmProvider provider = null;
		try
		{
			AppClient appClient = new AppClient();
			FieldList fieldList = EmaFactory.createFieldList();

			OmmIProviderConfig config = EmaFactory.createOmmIProviderConfig();
			
			provider = EmaFactory.createOmmProvider(config, appClient);
			
			while(appClient.itemHandle == 0) Thread.sleep(1000);
				
			for( int i = 0; i < 10; i++ )
			{
				fieldList.clear();
				fieldList.add(EmaFactory.createFieldEntry().real(22, 3991 + i, OmmReal.MagnitudeType.EXPONENT_NEG_2));
				fieldList.add(EmaFactory.createFieldEntry().real(30, 10 + i, OmmReal.MagnitudeType.EXPONENT_0));
				
				provider.submit( EmaFactory.createUpdateMsg().payload( fieldList ), appClient.itemHandle );
				
				Thread.sleep(1000);
			}
			
			appClient.processUpdateGroupStatus( provider );

			Thread.sleep(60000);
		} 
		catch (InterruptedException | OmmException excp)
		{
			System.out.println(excp.getMessage());
		}
		finally 
		{
			if (provider != null) provider.uninitialize();
		}
	}
}
