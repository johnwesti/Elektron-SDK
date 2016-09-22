///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

#include "IProvider.h"

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
  sending a directory update with group status (using the domain Directoy helper class)
- all other domains are status closed
*/

using namespace thomsonreuters::ema::access;
using namespace thomsonreuters::ema::rdm;
using namespace thomsonreuters::ema::domain::directory;
using namespace std;

UInt64 itemHandle = 0;
EmaBuffer groupId( "1", 1 );

void AppClient::processLoginRequest( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	event.getProvider().submit(RefreshMsg().domainType(MMT_LOGIN).name(reqMsg.getName()).nameType(USER_NAME).complete()
		.solicited( true ).state( OmmState::OpenEnum, OmmState::OkEnum, OmmState::NoneEnum, "Login accepted" ),
		event.getHandle() );
}

void AppClient::processUpdateServiceGroup( OmmProvider& provider )
{
		Dir::Info info;
		info.serviceId(1).serviceName("DIRECT_FEED");
		
		Dir::Group group;
		group.groupId( groupId ).status( OmmState::ClosedEnum, OmmState::SuspectEnum, OmmState::NoneEnum, "" );
		
		Dir::Service service;
		service.info( info ).addGroup( group );
		
		Dir::Directory directory;
		directory.addService( service );
		
		provider.submit(UpdateMsg().domainType(MMT_DIRECTORY)
			.payload(directory.getData()) , 0 );
}

void AppClient::processMarketPriceRequest( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	if ( itemHandle != 0 )
	{
		processInvalidItemRequest(reqMsg, event);
		return;
	}

	event.getProvider().submit( RefreshMsg().domainType( MMT_MARKET_PRICE).name( reqMsg.getName() ).serviceName( reqMsg.getServiceName() ).solicited(true)
		.state( OmmState::OpenEnum, OmmState::OkEnum, OmmState::NoneEnum, "Refresh Completed" )
		.payload( FieldList()
			.addReal( 22, 3990, OmmReal::ExponentNeg2Enum )
			.addReal( 25, 3994, OmmReal::ExponentNeg2Enum )
			.addReal( 30, 9, OmmReal::Exponent0Enum )
			.addReal( 31, 19, OmmReal::Exponent0Enum )
			.complete() )
		.complete(), event.getHandle() );
	
	itemHandle = event.getHandle();
}

void AppClient::processInvalidItemRequest( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	event.getProvider().submit( StatusMsg().name( reqMsg.getName() ).serviceName( reqMsg.getServiceName() )
		.domainType( reqMsg.getDomainType() )
		.state( OmmState::ClosedEnum, OmmState::SuspectEnum, OmmState::NotFoundEnum, "Item not found" ),
		event.getHandle() );
}

void AppClient::onReqMsg( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	switch ( reqMsg.getDomainType() )
	{
	case MMT_LOGIN:
		processLoginRequest( reqMsg, event );
		break;
	case MMT_MARKET_PRICE:
		processMarketPriceRequest( reqMsg, event );
		break;
	default:
		processInvalidItemRequest( reqMsg, event );
		break;
	}
}

int main( int argc, char* argv[] )
{
	try
	{
		AppClient appClient;

		OmmProvider provider( OmmIProviderConfig(), appClient );
		
		while ( itemHandle == 0 ) sleep( 1000 );
		
		for ( Int32 i = 0; i < 10; i++ )
		{
			provider.submit( UpdateMsg().payload( FieldList()
					.addReal( 22, 3391 + i, OmmReal::ExponentNeg2Enum )
					.addReal( 30, 10 + i, OmmReal::Exponent0Enum )
					.complete() ), itemHandle );
					
			sleep( 1000 );
		}
		
		appClient.processUpdateServiceGroup( provider );

		sleep( 50000 );
		
	}
	catch ( const OmmException& excp )
	{
		cout << excp << endl;
	}
	
	return 0;
}

