import os, asyncio, aiohttp, json, time, redis
import sched
from bs4 import BeautifulSoup
#www.tianyancha.com
#http://www.xicidaili.com/nn/

schedule = sched.scheduler(time.time, time.sleep) 
loop = asyncio.get_event_loop()
sem = asyncio.Semaphore(10)

class Proxy:

	def __init__(self, ip, score):
		self.ip = ip
		self.score = score

	def __lt__(self, other):
		return self.score > other.score

	def __str__(self):
		return json.dumps(self.ip)

class ProxyEncoder(json.JSONEncoder):
	def default(self, o):
		if isinstance(o, Proxy):
			return o.__str__()
		return json.JSONEncoder.default(o)

class ProxyService:

	def __init__(self):

		pool = redis.ConnectionPool(host="192.168.10.208", port=45000, decode_responses=True)
		self._redis = redis.Redis(connection_pool=pool)

		self._proxyUrls = "http://www.xicidaili.com/nn/", 

		self._validateUrl = "https://www.baidu.com/"

		self._headers = {
			"User-Agent" : "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
		}

	async def allips(self):
		conn = aiohttp.TCPConnector(ssl=False, limit=200, use_dns_cache=True)
		async with aiohttp.ClientSession(connector=conn) as session:
			async with session.get(self._proxyUrl,  headers=self._headers) as resp:
				assert resp.status == 200
				rs = json.loads(await resp.text())
				#print(html)
				arr = rs["obj"]
				arr.sort(key = lambda x:x["responseTime"])
				print(arr)
				return arr;

	async def tyc(self, ip, session):
		_proxy = 'http://' + ip["ip"] + ":" + str(ip["port"])
		#print(_proxy)
		try:
			start = time.time()
			async with session.get(self._validateUrl,  headers=self._headers, proxy=_proxy, timeout=5) as resp:
				assert resp.status == 200
				await resp.text()
				end = time.time()
				print('proxy: %s' % _proxy, 'Used time-->', end - start, 's')
				return Proxy(ip, end - start)
		except Exception as e:
			return Proxy(ip, 99999)
			

	async def elect(self):
		ips = await self.allips()
		async with aiohttp.ClientSession() as session:
			return await asyncio.gather(*[self.tyc(ip, session) for ip in ips])

	def run(self):		
		proxies = loop.run_until_complete(self.elect())
		proxies = list(filter(lambda x: x.score < 0.5, proxies))
		proxies.sort(reverse=True)
		p = proxies[0]
		self._redis.set("thanos_proxy", "http://" + p.ip["ip"] + ":" + str(p.ip["port"]))


def run_proxy(p, inc):
	p.run()
	schedule.enter(inc, 0, run_proxy, (p, inc))  

if __name__ == '__main__':
	p = ProxyService()

	p.run()
	# schedule.enter(0, 0, run_proxy, (p, 120))
	# schedule.run()
