///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2016. All rights reserved.            --
///*|-----------------------------------------------------------------------------

package com.thomsonreuters.ema.examples.training.iprovider.series100.example151__Login__AcceptDeny;

/*
assumptions:
- dependence on the standard / default configuration; e.g.
  - no EmaConfig.xml file
  - standard RDM file dictionary located in the same directory as the app
  - directory requests processed by the EMA
  - dictionary requests processed by the EMA

this interactive provider shows the following functionality
- accepting login requests using the domain Login helper class
- denying login request if not user name
- all other domains are status closed
*/

import com.thomsonreuters.ema.access.EmaFactory;
import com.thomsonreuters.ema.access.GenericMsg;
import com.thomsonreuters.ema.access.Msg;
import com.thomsonreuters.ema.access.OmmException;
import com.thomsonreuters.ema.access.OmmIProviderConfig;
import com.thomsonreuters.ema.access.OmmProvider;
import com.thomsonreuters.ema.access.OmmProviderClient;
import com.thomsonreuters.ema.access.OmmProviderEvent;
import com.thomsonreuters.ema.access.OmmState;
import com.thomsonreuters.ema.access.PostMsg;
import com.thomsonreuters.ema.access.RefreshMsg;
import com.thomsonreuters.ema.access.ReqMsg;
import com.thomsonreuters.ema.access.StatusMsg;
import com.thomsonreuters.ema.domain.DomainFactory;
import com.thomsonreuters.ema.domain.login.Log;
import com.thomsonreuters.ema.rdm.EmaRdm;


class AppClient implements OmmProviderClient
{	
	public void onReqMsg(ReqMsg reqMsg, OmmProviderEvent event)
	{
		switch (reqMsg.domainType())
		{
			case EmaRdm.MMT_LOGIN :
				processLoginRequest(reqMsg, event);
				break;
			default :
				processInvalidDomainRequest(reqMsg, event);
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
		if ( !reqMsg.name().equals("user") )
		{
			event.provider().submit( EmaFactory.createStatusMsg().domainType(EmaRdm.MMT_LOGIN).name(reqMsg.name()).nameType(reqMsg.nameType())
					.state(OmmState.StreamState.CLOSED, OmmState.DataState.SUSPECT, OmmState.StatusCode.NOT_AUTHORIZED, "Login denied"),
					event.handle() );
		}
		else
		{
			Log.RefreshAttrib refreshAttrib = DomainFactory.Login.createRefreshAttrib();

			Log.ReqAttrib reqAttrib = DomainFactory.Login.createReqAttrib( reqMsg.attrib().elementList() );
			
			if(reqAttrib.hasAllowSuspectData())
				refreshAttrib.allowSuspectData(reqAttrib.allowSuspectData());
			if(reqAttrib.hasSingleOpen())
				refreshAttrib.singleOpen(reqAttrib.singleOpen());

			if(reqAttrib.hasPosition())
				refreshAttrib.position( reqAttrib.position() );
			if(reqAttrib.hasApplicationId())
				refreshAttrib.applicationId( reqAttrib.applicationId() );
			
			event.provider().submit( EmaFactory.createRefreshMsg().domainType(EmaRdm.MMT_LOGIN).name(reqMsg.name()).nameType(EmaRdm.USER_NAME)
					.complete(true).attrib(refreshAttrib.data()).solicited(true)
					.state(OmmState.StreamState.OPEN, OmmState.DataState.OK, OmmState.StatusCode.NONE, "Login accepted"),
					event.handle() );
		}
	}
	
	void processInvalidDomainRequest(ReqMsg reqMsg, OmmProviderEvent event)
	{
		event.provider().submit( EmaFactory.createStatusMsg().name(reqMsg.name()).serviceName(reqMsg.serviceName())
				.state(OmmState.StreamState.CLOSED, OmmState.DataState.SUSPECT,	OmmState.StatusCode.NOT_FOUND, "Invalid domain"),
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
			
			OmmIProviderConfig config = EmaFactory.createOmmIProviderConfig();
			
			provider = EmaFactory.createOmmProvider(config.port("14002"), appClient);
			
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
