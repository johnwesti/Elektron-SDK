///*|-----------------------------------------------------------------------------
// *|            This source code is provided under the Apache 2.0 license      --
// *|  and is provided AS IS with no warranty or guarantee of fit for purpose.  --
// *|                See the project's LICENSE.md for details.                  --
// *|           Copyright Thomson Reuters 2015. All rights reserved.            --
///*|-----------------------------------------------------------------------------

#include "IProvider.h"

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

using namespace thomsonreuters::ema::access;
using namespace thomsonreuters::ema::rdm;
using namespace thomsonreuters::ema::domain::login;
using namespace std;

void AppClient::processLoginRequest( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	if ( reqMsg.getNameType() != USER_NAME || reqMsg.getName() != "user" )
	{
		event.getProvider().submit(StatusMsg().domainType(MMT_LOGIN).name(reqMsg.getName()).nameType(USER_NAME)
			.state( OmmState::ClosedEnum, OmmState::SuspectEnum, OmmState::StatusCode::NotAuthorizedEnum, "Login denied" ),
			event.getHandle() );
	}
	else
	{
		RefreshMsg loginRefresh;
		Login::RefreshAttrib refreshAttrib;
		
		Login::ReqAttrib reqAttrib( reqMsg.getAttrib().getElementList() );

		if ( reqAttrib.hasAllowSuspectData() )
			refreshAttrib.allowSuspectData( reqAttrib.getAllowSuspectData() );
		
		if ( reqAttrib.hasSingleOpen() )
			refreshAttrib.singleOpen( reqAttrib.getSingleOpen() );
		
		if ( reqAttrib.hasPosition() )
			refreshAttrib.position( reqAttrib.getPosition() );
		
		if ( reqAttrib.hasApplicationId() )
			refreshAttrib.applicationId( reqAttrib.getApplicationId() );

		event.getProvider().submit( loginRefresh.domainType( MMT_LOGIN ).name( reqMsg.getName() ).nameType( USER_NAME )
			.attrib(refreshAttrib.getData()).solicited(true).complete()
			.state( OmmState::OpenEnum, OmmState::OkEnum, OmmState::NoneEnum, "Login accepted" ),
			event.getHandle() );
	}
}

void AppClient::processInvalidDomainRequest( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	event.getProvider().submit( StatusMsg().name( reqMsg.getName() ).serviceName( reqMsg.getServiceName() )
		.domainType( reqMsg.getDomainType() )
		.state( OmmState::ClosedEnum, OmmState::SuspectEnum, OmmState::NotFoundEnum, "Invalid domain" ),
		event.getHandle() );
}

void AppClient::onReqMsg( const ReqMsg& reqMsg, const OmmProviderEvent& event )
{
	switch ( reqMsg.getDomainType() )
	{
	case MMT_LOGIN :
		processLoginRequest( reqMsg, event );
		break;
	default :
		processInvalidDomainRequest( reqMsg, event );
		break;
	}
}

int main( int argc, char* argv[] )
{
	try
	{
		AppClient appClient;

		OmmProvider provider( OmmIProviderConfig().port( "14002" ), appClient );

		sleep( 60000 );
	}
	catch ( const OmmException& excp )
	{
		cout << excp << endl;
	}

	return 0;
}
