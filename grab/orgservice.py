import os, asyncio, aiohttp
import re, redis, json, time
import urllib.parse
from bs4 import BeautifulSoup
from fontTools.ttLib import TTFont
import threading

loop = asyncio.get_event_loop()
#asyncio.Semaphore(),限制同时运行协程数量
sem = asyncio.Semaphore(8)

class OrgServiceI:

	def __init__(self):
		# redis
		pool = redis.ConnectionPool(host="192.168.10.208", port=45000, decode_responses=True)
		self._redis = redis.Redis(connection_pool=pool)
		#self._host = "www.qichacha.com"
		self._host = "www.tianyancha.com"
		self._url = "https://%s/search?key=%s" % (self._host, "")
		self._referer = "https://%s" % (self._host)
		#TODO: make it automatic
		#auth_token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODkwMjI5NTYxNiIsImlhdCI6MTUzMjA3NjI0MiwiZXhwIjoxNTQ3NjI4MjQyfQ.ZTPE7eq0xMsPn5KStuEkI9FuqLYgianxWT9fY8pyHfyGPUaoKkIzaVgwg1TXrkJhwjMCHDIt765jgpxxON1GGQ"
		self._cookie = 'auth_token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODkwMjI5NTYxNiIsImlhdCI6MTUzMjA3NjI0MiwiZXhwIjoxNTQ3NjI4MjQyfQ.ZTPE7eq0xMsPn5KStuEkI9FuqLYgianxWT9fY8pyHfyGPUaoKkIzaVgwg1TXrkJhwjMCHDIt765jgpxxON1GGQ'
		self._fontCachedPath = "./fonts/%s.woff"
		#_session = "lnpi16dbjvd83ftb43atc8lg77"
		#_zgDid = "%7B%22did%22%3A%20%22164a6dcfcc71291-09ff3ca9947395-5b193413-1fa400-164a6dcfcc879%22%7D"
		#self._cookie = "PHPSESSID=%s;zg_did=%s;" % (_session, _zgDid)
		#get PHPSESSID
		self._useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
		self._fontUrl = "https://static.tianyancha.com/fonts-styles/fonts/%s/%s/tyc-num.woff"
		self._headers = {
			"Accept" : "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
			"Accept-Encoding" : "gzip, deflate, br",
			#"Host" : self._host,
			#"Connection" : "keep-alive",
			"Referer" : self._referer,
			"Cookie" : self._cookie,
			"User-Agent" : self._useragent
		}
		self._symbol = "："
		self._fields = {
			"注册资本" : "N/A",
			"注册时间" : "N/A",
			"公司状态" : "N/A",
			"法人" : "N/A"
		}

		self._re = re.compile(r'/company/[^/]+')

		self._secret = loop.run_until_complete(self.downloadFont())
		# event_loop.close()
		#1985-09-15
		#8076-40-89
		#3990813.182
		#5004785.872

		#2018-0 -12
		#2487-41-82	
	# def resetCookie(self):
	# 	self._headers["Cookie"] = self._headers["Cookie"] + str(int(time.time()))

	async def getQuery(self, name=None, url=None):
		# self.resetCookie()
		async with sem:
			async with aiohttp.ClientSession() as session:
				if name:
					async with session.get(self._url + urllib.parse.quote(name),  headers=self._headers) as resp:
						assert resp.status == 200
						html = await resp.text()
						soup = BeautifulSoup(html, 'lxml')
						[s.extract() for s in soup.find_all("img")]
						aTag = soup.find("a", text=name)
						if aTag:
							#self.resetCookie()
							async with session.get(aTag["href"],  headers=self._headers) as _resp:
								assert _resp.status == 200
								return await _resp.text()
						else:
							return False
				if url and re.search(self._re, url):
					print(url)
					async with session.get(url,  headers=self._headers) as _resp:
						assert _resp.status == 200
						return await _resp.text()
		

	async def downloadFont(self):
		# self.resetCookie()
		async with aiohttp.ClientSession() as session:
			async with session.get("https://" + self._host,  headers=self._headers) as resp:
				assert resp.status == 200
				html = await resp.text()
				soup = BeautifulSoup(html, 'lxml')
				[s.extract() for s in soup.find_all("img")]
				bodyClass = soup.body["class"][0].replace("font-", "")
				print(bodyClass)
				file = self._fontCachedPath % (bodyClass)
				if not os.path.exists(file):
					#self.resetCookie()
					async with session.get(self._fontUrl % (bodyClass[0:2], bodyClass),  headers=self._headers) as _resp:
						assert _resp.status == 200
						with open(file, 'wb') as fd:
							while True:
								chunk = await _resp.content.read(1024)
								if not chunk:
									break
								fd.write(chunk)
						font = TTFont(file)
						#font.saveXML("./fonts/1.xml")
						gly_list = font.getGlyphOrder()
						gly_names = font.getGlyphNames()
						gly_list = gly_list[2:12]
						gly_names = gly_names[0:10]
						secrets = {}
						for i in range(10):
							secrets[gly_list[i]] = gly_names[i]
						# for number, gly in enumerate(gly_list):
						# 	# print(str(number) + ":" + gly)
						# 	if re.match(r'\d+', gly):
						# 		secrets[gly] = str(number)
						self._redis.set(bodyClass, json.dumps(secrets))
						return secrets
				else:
					return json.loads(self._redis.get(bodyClass))

	def sub(self, str):
		return re.sub(r'\|[^\|]+', '', str)

	def convert(self, num):
		num = re.sub(r'[^\d\.-]+', '', num)
		str = ''
		for n in list(num):
			str += self._secret[n] if n in self._secret else n
		return str

	def parse(self, html):
		soup = BeautifulSoup(html, 'lxml')
		[s.extract() for s in soup.find_all("img")]
		#print(soup.find("div", class_="boss-td").get_text("|", strip=True))
		faren = soup.find("div", class_="humancompany")
		if faren:
		#print(faren.get_text("|", strip=True))
			self._fields["法人"] = self.sub(faren.get_text("|", strip=True))
			#registered capital
			td = faren.find_parent("td")
			tr = faren.find_parent("tr")
		else:
			td = soup.find("td", attrs={"tyc-event-ch" : "CompangyDetail.faren"})
			tr = td.find_parent("tr")
		capital = td.find_next_sibling().find("text", class_="tyc-num lh24")
		if capital:
			self._fields["注册资本"] = self.convert(capital.get_text(strip=True))
		tr = tr.find_next_sibling()
		founded = tr.find("text", class_="tyc-num lh24")
		if founded:
			self._fields["注册时间"] = self.convert(founded.get_text(strip=True))
		tr = tr.find_next_sibling()
		self._fields["公司状态"] = tr.find("div", class_="num-opening").get_text(strip=True)
		#
		table = tr.find_parent("table").find_next_sibling()
		# del useless td
		table.find("td", class_="sort-bg").extract()
		for index, row in enumerate(table.find_all("td"), start=0):
			if index % 2 == 0:
				if row.find_next_sibling().find("text", class_="tyc-num lh24"):
					self._fields[self.sub(row.get_text("|", strip=True))] = self.convert(row.find_next_sibling().get_text(strip=True))
				else:
					self._fields[self.sub(row.get_text("|", strip=True))] = self.sub(row.find_next_sibling().get_text("|", strip=True))
		
		print(self._fields)
		#主要人员
		zyry = soup.find("div", attrs={"tyc-event-ch" : "CompangyDetail.zhuyaorenyuan"})
		if zyry:
			tbody = soup.find("div", attrs={"tyc-event-ch" : "CompangyDetail.zhuyaorenyuan"}).find("tbody")
			relatedPerson = {}
			for tr in tbody.find_all("tr"):
				tds = tr.find_all("td")
				relatedPerson[self.sub(tds[1].a.get_text("|", strip=True))] = self.sub(tds[2].get_text("|", strip=True))
			print(relatedPerson)	

		#股东信息
		ch = soup.find("div", id="_container_holder")
		if ch:
			hoders, url = [], []
			tbody = ch.find("tbody")
			for tr in tbody.find_all("tr"):
				tds = tr.find_all("td")
				hoders.append({"name" : self.sub(tds[1].a.get_text("|", strip=True)), "rate": self.sub(tds[2].get_text("|", strip=True)), 
				"scc" : tds[3].get_text("|", strip=True)})
				url.append(tds[1].a["href"])

			t = threading.Thread(target=self.analyse, args=(url,))
			t.start()

	def analyse(self, urls):
		_loop = asyncio.new_event_loop()
		asyncio.set_event_loop(_loop)
		_loop.run_until_complete(asyncio.gather(*[self.parseOrgInfo(url=u) for u in urls]))
		_loop.close()

	async def parseOrgInfo(self, name=None, url=None):
		result = await self.getQuery(name=name, url=url)
		if result:
			self.parse(result)

	def getOrgInfo(self, name=None, url=None):
		loop.run_until_complete(self.parseOrgInfo(name=name, url=url))
		#print(urls)
		#loop.run_until_complete(asyncio.gather(*[self.parseOrgInfo(url=u) for u in urls]))

				
if __name__ == '__main__':
	o = OrgServiceI()
	o.getOrgInfo(name="华为技术有限公司")
	#loop.close()		