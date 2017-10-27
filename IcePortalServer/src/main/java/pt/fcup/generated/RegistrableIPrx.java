// **********************************************************************
//
// Copyright (c) 2003-2017 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************
//
// Ice version 3.7.0
//
// <auto-generated>
//
// Generated from file `Registrable.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>
//

package pt.fcup.generated;

public interface RegistrableIPrx extends com.zeroc.Ice.ObjectPrx
{
    default boolean registerSeeder(String fileHash, String fileName, int fileSize, String protocol, int port, int videoSizeX, int videoSizeY, int bitrate)
    {
        return registerSeeder(fileHash, fileName, fileSize, protocol, port, videoSizeX, videoSizeY, bitrate, com.zeroc.Ice.ObjectPrx.noExplicitContext);
    }

    default boolean registerSeeder(String fileHash, String fileName, int fileSize, String protocol, int port, int videoSizeX, int videoSizeY, int bitrate, java.util.Map<String, String> context)
    {
        return _iceI_registerSeederAsync(fileHash, fileName, fileSize, protocol, port, videoSizeX, videoSizeY, bitrate, context, true).waitForResponse();
    }

    default java.util.concurrent.CompletableFuture<java.lang.Boolean> registerSeederAsync(String fileHash, String fileName, int fileSize, String protocol, int port, int videoSizeX, int videoSizeY, int bitrate)
    {
        return _iceI_registerSeederAsync(fileHash, fileName, fileSize, protocol, port, videoSizeX, videoSizeY, bitrate, com.zeroc.Ice.ObjectPrx.noExplicitContext, false);
    }

    default java.util.concurrent.CompletableFuture<java.lang.Boolean> registerSeederAsync(String fileHash, String fileName, int fileSize, String protocol, int port, int videoSizeX, int videoSizeY, int bitrate, java.util.Map<String, String> context)
    {
        return _iceI_registerSeederAsync(fileHash, fileName, fileSize, protocol, port, videoSizeX, videoSizeY, bitrate, context, false);
    }

    default com.zeroc.IceInternal.OutgoingAsync<java.lang.Boolean> _iceI_registerSeederAsync(String iceP_fileHash, String iceP_fileName, int iceP_fileSize, String iceP_protocol, int iceP_port, int iceP_videoSizeX, int iceP_videoSizeY, int iceP_bitrate, java.util.Map<String, String> context, boolean sync)
    {
        com.zeroc.IceInternal.OutgoingAsync<java.lang.Boolean> f = new com.zeroc.IceInternal.OutgoingAsync<>(this, "registerSeeder", null, sync, null);
        f.invoke(true, context, null, ostr -> {
                     ostr.writeString(iceP_fileHash);
                     ostr.writeString(iceP_fileName);
                     ostr.writeInt(iceP_fileSize);
                     ostr.writeString(iceP_protocol);
                     ostr.writeInt(iceP_port);
                     ostr.writeInt(iceP_videoSizeX);
                     ostr.writeInt(iceP_videoSizeY);
                     ostr.writeInt(iceP_bitrate);
                 }, istr -> {
                     boolean ret;
                     ret = istr.readBool();
                     return ret;
                 });
        return f;
    }

    default boolean deregisterSeeder(String deregMessage)
    {
        return deregisterSeeder(deregMessage, com.zeroc.Ice.ObjectPrx.noExplicitContext);
    }

    default boolean deregisterSeeder(String deregMessage, java.util.Map<String, String> context)
    {
        return _iceI_deregisterSeederAsync(deregMessage, context, true).waitForResponse();
    }

    default java.util.concurrent.CompletableFuture<java.lang.Boolean> deregisterSeederAsync(String deregMessage)
    {
        return _iceI_deregisterSeederAsync(deregMessage, com.zeroc.Ice.ObjectPrx.noExplicitContext, false);
    }

    default java.util.concurrent.CompletableFuture<java.lang.Boolean> deregisterSeederAsync(String deregMessage, java.util.Map<String, String> context)
    {
        return _iceI_deregisterSeederAsync(deregMessage, context, false);
    }

    default com.zeroc.IceInternal.OutgoingAsync<java.lang.Boolean> _iceI_deregisterSeederAsync(String iceP_deregMessage, java.util.Map<String, String> context, boolean sync)
    {
        com.zeroc.IceInternal.OutgoingAsync<java.lang.Boolean> f = new com.zeroc.IceInternal.OutgoingAsync<>(this, "deregisterSeeder", null, sync, null);
        f.invoke(true, context, null, ostr -> {
                     ostr.writeString(iceP_deregMessage);
                 }, istr -> {
                     boolean ret;
                     ret = istr.readBool();
                     return ret;
                 });
        return f;
    }

    default boolean sendHashes(String[] chunkHashes, String fileHash, String seederIP, int seederPort)
    {
        return sendHashes(chunkHashes, fileHash, seederIP, seederPort, com.zeroc.Ice.ObjectPrx.noExplicitContext);
    }

    default boolean sendHashes(String[] chunkHashes, String fileHash, String seederIP, int seederPort, java.util.Map<String, String> context)
    {
        return _iceI_sendHashesAsync(chunkHashes, fileHash, seederIP, seederPort, context, true).waitForResponse();
    }

    default java.util.concurrent.CompletableFuture<java.lang.Boolean> sendHashesAsync(String[] chunkHashes, String fileHash, String seederIP, int seederPort)
    {
        return _iceI_sendHashesAsync(chunkHashes, fileHash, seederIP, seederPort, com.zeroc.Ice.ObjectPrx.noExplicitContext, false);
    }

    default java.util.concurrent.CompletableFuture<java.lang.Boolean> sendHashesAsync(String[] chunkHashes, String fileHash, String seederIP, int seederPort, java.util.Map<String, String> context)
    {
        return _iceI_sendHashesAsync(chunkHashes, fileHash, seederIP, seederPort, context, false);
    }

    default com.zeroc.IceInternal.OutgoingAsync<java.lang.Boolean> _iceI_sendHashesAsync(String[] iceP_chunkHashes, String iceP_fileHash, String iceP_seederIP, int iceP_seederPort, java.util.Map<String, String> context, boolean sync)
    {
        com.zeroc.IceInternal.OutgoingAsync<java.lang.Boolean> f = new com.zeroc.IceInternal.OutgoingAsync<>(this, "sendHashes", null, sync, null);
        f.invoke(true, context, null, ostr -> {
                     ostr.writeStringSeq(iceP_chunkHashes);
                     ostr.writeString(iceP_fileHash);
                     ostr.writeString(iceP_seederIP);
                     ostr.writeInt(iceP_seederPort);
                 }, istr -> {
                     boolean ret;
                     ret = istr.readBool();
                     return ret;
                 });
        return f;
    }

    /**
     * Contacts the remote server to verify that the object implements this type.
     * Raises a local exception if a communication error occurs.
     * @param obj The untyped proxy.
     * @return A proxy for this type, or null if the object does not support this type.
     **/
    static RegistrableIPrx checkedCast(com.zeroc.Ice.ObjectPrx obj)
    {
        return com.zeroc.Ice.ObjectPrx._checkedCast(obj, ice_staticId(), RegistrableIPrx.class, _RegistrableIPrxI.class);
    }

    /**
     * Contacts the remote server to verify that the object implements this type.
     * Raises a local exception if a communication error occurs.
     * @param obj The untyped proxy.
     * @param context The Context map to send with the invocation.
     * @return A proxy for this type, or null if the object does not support this type.
     **/
    static RegistrableIPrx checkedCast(com.zeroc.Ice.ObjectPrx obj, java.util.Map<String, String> context)
    {
        return com.zeroc.Ice.ObjectPrx._checkedCast(obj, context, ice_staticId(), RegistrableIPrx.class, _RegistrableIPrxI.class);
    }

    /**
     * Contacts the remote server to verify that a facet of the object implements this type.
     * Raises a local exception if a communication error occurs.
     * @param obj The untyped proxy.
     * @param facet The name of the desired facet.
     * @return A proxy for this type, or null if the object does not support this type.
     **/
    static RegistrableIPrx checkedCast(com.zeroc.Ice.ObjectPrx obj, String facet)
    {
        return com.zeroc.Ice.ObjectPrx._checkedCast(obj, facet, ice_staticId(), RegistrableIPrx.class, _RegistrableIPrxI.class);
    }

    /**
     * Contacts the remote server to verify that a facet of the object implements this type.
     * Raises a local exception if a communication error occurs.
     * @param obj The untyped proxy.
     * @param facet The name of the desired facet.
     * @param context The Context map to send with the invocation.
     * @return A proxy for this type, or null if the object does not support this type.
     **/
    static RegistrableIPrx checkedCast(com.zeroc.Ice.ObjectPrx obj, String facet, java.util.Map<String, String> context)
    {
        return com.zeroc.Ice.ObjectPrx._checkedCast(obj, facet, context, ice_staticId(), RegistrableIPrx.class, _RegistrableIPrxI.class);
    }

    /**
     * Downcasts the given proxy to this type without contacting the remote server.
     * @param obj The untyped proxy.
     * @return A proxy for this type.
     **/
    static RegistrableIPrx uncheckedCast(com.zeroc.Ice.ObjectPrx obj)
    {
        return com.zeroc.Ice.ObjectPrx._uncheckedCast(obj, RegistrableIPrx.class, _RegistrableIPrxI.class);
    }

    /**
     * Downcasts the given proxy to this type without contacting the remote server.
     * @param obj The untyped proxy.
     * @param facet The name of the desired facet.
     * @return A proxy for this type.
     **/
    static RegistrableIPrx uncheckedCast(com.zeroc.Ice.ObjectPrx obj, String facet)
    {
        return com.zeroc.Ice.ObjectPrx._uncheckedCast(obj, facet, RegistrableIPrx.class, _RegistrableIPrxI.class);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the per-proxy context.
     * @param newContext The context for the new proxy.
     * @return A proxy with the specified per-proxy context.
     **/
    @Override
    default RegistrableIPrx ice_context(java.util.Map<String, String> newContext)
    {
        return (RegistrableIPrx)_ice_context(newContext);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the adapter ID.
     * @param newAdapterId The adapter ID for the new proxy.
     * @return A proxy with the specified adapter ID.
     **/
    @Override
    default RegistrableIPrx ice_adapterId(String newAdapterId)
    {
        return (RegistrableIPrx)_ice_adapterId(newAdapterId);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the endpoints.
     * @param newEndpoints The endpoints for the new proxy.
     * @return A proxy with the specified endpoints.
     **/
    @Override
    default RegistrableIPrx ice_endpoints(com.zeroc.Ice.Endpoint[] newEndpoints)
    {
        return (RegistrableIPrx)_ice_endpoints(newEndpoints);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the locator cache timeout.
     * @param newTimeout The new locator cache timeout (in seconds).
     * @return A proxy with the specified locator cache timeout.
     **/
    @Override
    default RegistrableIPrx ice_locatorCacheTimeout(int newTimeout)
    {
        return (RegistrableIPrx)_ice_locatorCacheTimeout(newTimeout);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the invocation timeout.
     * @param newTimeout The new invocation timeout (in seconds).
     * @return A proxy with the specified invocation timeout.
     **/
    @Override
    default RegistrableIPrx ice_invocationTimeout(int newTimeout)
    {
        return (RegistrableIPrx)_ice_invocationTimeout(newTimeout);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for connection caching.
     * @param newCache <code>true</code> if the new proxy should cache connections; <code>false</code> otherwise.
     * @return A proxy with the specified caching policy.
     **/
    @Override
    default RegistrableIPrx ice_connectionCached(boolean newCache)
    {
        return (RegistrableIPrx)_ice_connectionCached(newCache);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the endpoint selection policy.
     * @param newType The new endpoint selection policy.
     * @return A proxy with the specified endpoint selection policy.
     **/
    @Override
    default RegistrableIPrx ice_endpointSelection(com.zeroc.Ice.EndpointSelectionType newType)
    {
        return (RegistrableIPrx)_ice_endpointSelection(newType);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for how it selects endpoints.
     * @param b If <code>b</code> is <code>true</code>, only endpoints that use a secure transport are
     * used by the new proxy. If <code>b</code> is false, the returned proxy uses both secure and
     * insecure endpoints.
     * @return A proxy with the specified selection policy.
     **/
    @Override
    default RegistrableIPrx ice_secure(boolean b)
    {
        return (RegistrableIPrx)_ice_secure(b);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the encoding used to marshal parameters.
     * @param e The encoding version to use to marshal request parameters.
     * @return A proxy with the specified encoding version.
     **/
    @Override
    default RegistrableIPrx ice_encodingVersion(com.zeroc.Ice.EncodingVersion e)
    {
        return (RegistrableIPrx)_ice_encodingVersion(e);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for its endpoint selection policy.
     * @param b If <code>b</code> is <code>true</code>, the new proxy will use secure endpoints for invocations
     * and only use insecure endpoints if an invocation cannot be made via secure endpoints. If <code>b</code> is
     * <code>false</code>, the proxy prefers insecure endpoints to secure ones.
     * @return A proxy with the specified selection policy.
     **/
    @Override
    default RegistrableIPrx ice_preferSecure(boolean b)
    {
        return (RegistrableIPrx)_ice_preferSecure(b);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the router.
     * @param router The router for the new proxy.
     * @return A proxy with the specified router.
     **/
    @Override
    default RegistrableIPrx ice_router(com.zeroc.Ice.RouterPrx router)
    {
        return (RegistrableIPrx)_ice_router(router);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for the locator.
     * @param locator The locator for the new proxy.
     * @return A proxy with the specified locator.
     **/
    @Override
    default RegistrableIPrx ice_locator(com.zeroc.Ice.LocatorPrx locator)
    {
        return (RegistrableIPrx)_ice_locator(locator);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for collocation optimization.
     * @param b <code>true</code> if the new proxy enables collocation optimization; <code>false</code> otherwise.
     * @return A proxy with the specified collocation optimization.
     **/
    @Override
    default RegistrableIPrx ice_collocationOptimized(boolean b)
    {
        return (RegistrableIPrx)_ice_collocationOptimized(b);
    }

    /**
     * Returns a proxy that is identical to this proxy, but uses twoway invocations.
     * @return A proxy that uses twoway invocations.
     **/
    @Override
    default RegistrableIPrx ice_twoway()
    {
        return (RegistrableIPrx)_ice_twoway();
    }

    /**
     * Returns a proxy that is identical to this proxy, but uses oneway invocations.
     * @return A proxy that uses oneway invocations.
     **/
    @Override
    default RegistrableIPrx ice_oneway()
    {
        return (RegistrableIPrx)_ice_oneway();
    }

    /**
     * Returns a proxy that is identical to this proxy, but uses batch oneway invocations.
     * @return A proxy that uses batch oneway invocations.
     **/
    @Override
    default RegistrableIPrx ice_batchOneway()
    {
        return (RegistrableIPrx)_ice_batchOneway();
    }

    /**
     * Returns a proxy that is identical to this proxy, but uses datagram invocations.
     * @return A proxy that uses datagram invocations.
     **/
    @Override
    default RegistrableIPrx ice_datagram()
    {
        return (RegistrableIPrx)_ice_datagram();
    }

    /**
     * Returns a proxy that is identical to this proxy, but uses batch datagram invocations.
     * @return A proxy that uses batch datagram invocations.
     **/
    @Override
    default RegistrableIPrx ice_batchDatagram()
    {
        return (RegistrableIPrx)_ice_batchDatagram();
    }

    /**
     * Returns a proxy that is identical to this proxy, except for compression.
     * @param co <code>true</code> enables compression for the new proxy; <code>false</code> disables compression.
     * @return A proxy with the specified compression setting.
     **/
    @Override
    default RegistrableIPrx ice_compress(boolean co)
    {
        return (RegistrableIPrx)_ice_compress(co);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for its connection timeout setting.
     * @param t The connection timeout for the proxy in milliseconds.
     * @return A proxy with the specified timeout.
     **/
    @Override
    default RegistrableIPrx ice_timeout(int t)
    {
        return (RegistrableIPrx)_ice_timeout(t);
    }

    /**
     * Returns a proxy that is identical to this proxy, except for its connection ID.
     * @param connectionId The connection ID for the new proxy. An empty string removes the connection ID.
     * @return A proxy with the specified connection ID.
     **/
    @Override
    default RegistrableIPrx ice_connectionId(String connectionId)
    {
        return (RegistrableIPrx)_ice_connectionId(connectionId);
    }

    static String ice_staticId()
    {
        return "::generated::RegistrableI";
    }
}
