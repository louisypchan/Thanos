import os, asyncio, aiohttp
import svc, org, re, redis, json, time
import urllib.parse
from bs4 import BeautifulSoup
from fontTools.ttLib import TTFont

class OrgServiceI(org.OrgService):

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
		self._cookie = 'tyc-user-info=%257B%2522token%2522%253A%2522eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODkwMjI5NTYxNiIsImlhdCI6MTUzMjA3NjI0MiwiZXhwIjoxNTQ3NjI4MjQyfQ.ZTPE7eq0xMsPn5KStuEkI9FuqLYgianxWT9fY8pyHfyGPUaoKkIzaVgwg1TXrkJhwjMCHDIt765jgpxxON1GGQ%2522%252C%2522integrity%2522%253A%25220%2525%2522%252C%2522state%2522%253A%25220%2522%252C%2522redPoint%2522%253A%25220%2522%252C%2522vipManager%2522%253A%25220%2522%252C%2522vnum%2522%253A%25220%2522%252C%2522onum%2522%253A%25220%2522%252C%2522mobile%2522%253A%252218902295616%2522%257D;aliyungf_tc=AQAAACCx3DVCBwYAb1kwt9CxTnQNwDzr; csrfToken=omIsuUyFsX52QzkVVI4joAjj; TYCID=5fa749108bcf11e89ad1e1db4819121f; undefined=5fa749108bcf11e89ad1e1db4819121f; _ga=GA1.2.1846858651.1532058338; _gid=GA1.2.1129855011.1532058338; Hm_lvt_e92c8d65d92d534b0fc290df538b4758=1531899961,1531985933; RTYCID=f2f4fff83df743128636d7b37683cd66; bannerFlag=true; token=642d8bb3f38446e3b5e9b4783aac812a; _utm=d69c2724bb704c08a3c424dd5a567710; auth_token=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODkwMjI5NTYxNiIsImlhdCI6MTUzMjA3NjI0MiwiZXhwIjoxNTQ3NjI4MjQyfQ.ZTPE7eq0xMsPn5KStuEkI9FuqLYgianxWT9fY8pyHfyGPUaoKkIzaVgwg1TXrkJhwjMCHDIt765jgpxxON1GGQ; Hm_lpvt_e92c8d65d92d534b0fc290df538b4758='
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
			# "统一信用代码" : "N/A",
			# "纳税人识别号" : "N/A",
			# "工商注册号" : "N/A",
			# "组织机构代码": "N/A",
			# "公司类型" : "N/A",
			# "行业" : "N/A",
			# "登记机关" : "N/A",
			# "所属地区" : "N/A",
			# "曾用名" : "N/A",
			# "核准日期" : "N/A",
			# "营业期限" : "N/A",
			# "企业地址" : "N/A",
			"法人" : "N/A"
		}
		loop = asyncio.get_event_loop()
		self._secret = loop.run_until_complete(self.downloadFont())
		# event_loop.close()
		#1985-09-15
		#8076-40-89
		#3990813.182
		#5004785.872

		#2018-0 -12
		#2487-41-82	
	def resetCookie(self):
		self._headers["Cookie"] = self._headers["Cookie"] + str(int(time.time()))

	async def getQuery(self, name):
		_name = urllib.parse.quote(name)
		self.resetCookie()
		async with aiohttp.ClientSession() as session:
			async with session.get(self._url + _name,  headers=self._headers) as resp:
				assert resp.status == 200
				html = await resp.text()
				soup = BeautifulSoup(html, 'lxml')
				[s.extract() for s in soup.find_all("img")]
				aTag = soup.find("a", text=name)
				if aTag:
					self.resetCookie()
					async with session.get(aTag["href"],  headers=self._headers) as _resp:
						assert _resp.status == 200
						return await _resp.text()
				else:
					return -1

	async def downloadFont(self):
		self.resetCookie()
		async with aiohttp.ClientSession() as session:
			async with session.get("https://" + self._host,  headers=self._headers) as resp:
				assert resp.status == 200
				html = await resp.text()
				soup = BeautifulSoup(html, 'lxml')
				[s.extract() for s in soup.find_all("img")]
				bodyClass = soup.body["class"][0].replace("font-", "")
				file = self._fontCachedPath % (bodyClass)
				if not os.path.exists(file):
					self.resetCookie()
					async with session.get(self._fontUrl % (bodyClass[0:2], bodyClass),  headers=self._headers) as _resp:
						assert _resp.status == 200
						with open(file, 'wb') as fd:
							while True:
								chunk = await _resp.content.read(1024)
								if not chunk:
									break
								fd.write(chunk)
						font = TTFont(file)
						gly_list = font.getGlyphOrder()
						gly_list = gly_list[2:12]
						secrets = {}
						for number, gly in enumerate(gly_list):
							# print(str(number) + ":" + gly)
							secrets[gly] = str(number)
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

	def getOrgInfo(self, name, context=None):
		event_loop = asyncio.get_event_loop()
		result = event_loop.run_until_complete(self.getQuery(name))
		if result != -1:
			soup = BeautifulSoup(result, 'lxml')
			[s.extract() for s in soup.find_all("img")]
			#print(soup.find("div", class_="boss-td").get_text("|", strip=True))
			faren = soup.find("div", class_="humancompany")
			#print(faren.get_text("|", strip=True))
			self._fields["法人"] = self.sub(faren.get_text("|", strip=True))
			#registered capital
			td = faren.find_parent("td")
			tr = faren.find_parent("tr")
			self._fields["注册资本"] = self.convert(td.find_next_sibling().find("text", class_="tyc-num lh24").get_text(strip=True))
		#event_loop.close()
			tr = tr.find_next_sibling()
			self._fields["注册时间"] = self.convert(tr.find("text", class_="tyc-num lh24").get_text(strip=True))
			tr = tr.find_next_sibling()
			self._fields["公司状态"] = tr.find("div", class_="num-opening").get_text(strip=True)
			#
			table = tr.find_parent("table").find_next_sibling()
			# del useless td
			table.find("td", class_="sort-bg").extract()
			for index, row in enumerate(table.find_all("td"), start=0):
				if index % 2 == 0:
					self._fields[self.sub(row.get_text("|", strip=True))] = self.sub(row.find_next_sibling().get_text("|", strip=True))
			#print([row.get_text(strip=True) for index, row in enumerate(table.find_all("td"), start=0) if index % 2 == 0])
			
			#主要人员
			tbody = soup.find("div", attrs={"tyc-event-ch" : "CompangyDetail.zhuyaorenyuan"}).find("tbody")
			relatedPerson = {}
			for tr in tbody.find_all("tr"):
				tds = tr.find_all("td")
				relatedPerson[self.sub(tds[1].a.get_text("|", strip=True))] = self.sub(tds[2].get_text("|", strip=True))
			print(relatedPerson)

			#股东信息
			
				

o = OrgServiceI()
o.getOrgInfo("华为技术有限公司")		