/*
// Match based on path
def requestPath = mockRequest.getPath()
log.info "Path: "+ requestPath

if( requestPath.contains("json") )
{
    // return the name of the response you want to dispatch
    return "JSON Response"
}


// Match based on query parameter
def queryString = mockRequest.getRequest().getQueryString()
log.info "QueryString: " + queryString

if( queryString.contains("stockholm") )
{
    // return the name of the response you want to dispatch
    return "Response Stockholm"
}
else if( queryString.contains("london") )
{
    // return the name of the response you want to dispatch
    return "Response London"
}


// Match based on header
def acceptEncodingHeadeList = mockRequest.getRequestHeaders().get("Accept-Encoding")
log.info "AcceptEncodig Header List: " + acceptEncodingHeadeList

if( acceptEncodingHeadeList.contains("gzip,deflate") )
{
    // return the name of the response you want to dispatch
    return "GZiped Response"
}

*/
