/*
// Script dispatcher is used to select a response based on the incoming request.
// Here are few examples showing how to match based on path, query param, header and body

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
def acceptEncodingHeaderList = mockRequest.getRequestHeaders().get("Accept-Encoding")
log.info "AcceptEncoding Header List: " + acceptEncodingHeaderList

if( acceptEncodingHeaderList.contains("gzip,deflate") )
{
    // return the name of the response you want to dispatch
    return "GZiped Response"
}


// Match based on body
def requestBody = mockRequest.getRequestContent()
log.info "Request body: " + requestBody

if( requestBody.contains("some data") )
{
    // return the name of the response you want to dispatch
    return "Response N"
}
*/
