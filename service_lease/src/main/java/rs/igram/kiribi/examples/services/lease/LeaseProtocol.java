package com.kiribi.services.lease;

public final class LeaseProtocol {
	// 30 seconds
	public static final long DEFAULT_LEASE_DURATION = 30*1000;
	
	public static final byte CLIENT_REQUEST_RENEW	 = 20;
	public static final byte CLIENT_REQUEST_CANCEL  = 21;
	public static final byte SERVICE_RESPONSE_RENEWED 	= 20;
	public static final byte SERVICE_RESPONSE_CANCELED = 21;
}