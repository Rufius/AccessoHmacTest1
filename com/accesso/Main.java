package com.accesso;

import org.w3c.dom.Document;

public class Main
{
	public static void main(String[] args)
	{
		String xml = "<SERVICE request_type='GetProduct' merchant_id='100' reseller_id='16' auth_id='1' auth_timestamp='2016-08-05T13:05:56Z' />";

		Document request = ExampleHMAC.buildHMACRequest(xml);

		System.out.println(ExampleHMAC.toString(request));
	}
}
