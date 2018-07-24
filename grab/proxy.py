import os, asyncio, aiohttp, json, time
#www.tianyancha.com
#http://www.xiongmaodaili.com/freeproxy

class Proxy:

	def __init__(self):

		self._proxyUrl = "http://www.xiongmaodaili.com/xiongmao-web/freeip/list"

		self._validateUrl = "https://www.tianyancha.com"

		self.conn = aiohttp.TCPConnector(ssl=False, limit=100, use_dns_cache=True)

		self._headers = {
			"User-Agent" : "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
		}

	async def allips(self):
		async with aiohttp.ClientSession(connector=self.conn) as session:
			async with session.get(self._proxyUrl,  headers=self._headers) as resp:
				assert resp.status == 200
				rs = json.loads(await resp.text())
				#print(html)
				arr = rs["obj"]
				arr.sort(key = lambda x:x["responseTime"])
				return arr;

	async def elect(self):
		ips = await self.allips()
		async with aiohttp.ClientSession(connector=self.conn) as session:
			 for ip in ips:
			 	_proxy = 'http://' + ip["ip"] + ":" + str(ip["port"])
			 	start = time.time()
			 	async with session.get(self._validateUrl,  headers=self._headers, proxy=_proxy, timeout=5) as resp:
			 		assert resp.status == 200





if __name__ == '__main__':
	loop = asyncio.get_event_loop()