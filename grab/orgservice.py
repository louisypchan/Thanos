import asyncio, aiohttp
import svc, org, re
import urllib.parse
from bs4 import BeautifulSoup

class OrgServiceI(org.OrgService):

	def __init__(self):
		# redis
		#pool = redis.ConnectionPool(host="192.168.10.208", port=45000, decode_responses=True)
		#self._redis = redis.Redis(connection_pool=pool)
		#self._host = "www.qichacha.com"
		self._host = "www.tianyancha.com"
		self._url = "https://%s/search?key=%s" % (self._host, "")
		self._referer = "https://%s" % (self._host)
		#TODO: make it automatic
		auth_token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxODkwMjI5NTYxNiIsImlhdCI6MTUzMTk4NTk1NiwiZXhwIjoxNTQ3NTM3OTU2fQ.Oi5_p6V-kjN_p_SnwtFb6FnFAcab2DxRgRH0SO15I3zs3j2huS519fxSsAa3gHoVct8wFsB-YPktMrGsENucGg"
		self._cookie = "auth_token=%s" % (auth_token)
		#_session = "lnpi16dbjvd83ftb43atc8lg77"
		#_zgDid = "%7B%22did%22%3A%20%22164a6dcfcc71291-09ff3ca9947395-5b193413-1fa400-164a6dcfcc879%22%7D"
		#self._cookie = "PHPSESSID=%s;zg_did=%s;" % (_session, _zgDid)
		#get PHPSESSID
		self._useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
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

		#1985-09-15
		#8076-40-89
		
		#3990813.182
		#5004785.872

		#2018-0 -12
		#2487-41-82

		self._secrets = {
			"8" : "1",
			"0" : "9",
			"7" : "8",
			"6" : "7",
			"4" : "0",
			"9" : "5",
			"5" : "3",
			"2" : "2",
			"1" : "6",
			"3" : "4",
			"." : ".",
			"-" : "-"
 		}

	async def getQuery(self, name):
		_name = urllib.parse.quote(name)
		async with aiohttp.ClientSession() as session:
			async with session.get(self._url + _name,  headers=self._headers) as resp:
				assert resp.status == 200
				html = await resp.text()
				soup = BeautifulSoup(html, 'lxml')
				aTag = soup.find("a", text=name)
				if aTag:
					async with session.get(aTag["href"],  headers=self._headers) as _resp:
						assert _resp.status == 200
						return await _resp.text()
				else:
					return -1

	def sub(self, str):
		return re.sub(r'\|[^\|]+', '', str)


	def plain(self, num):
		num = re.sub(r'[^\d\.-]+', '', num)
		str = ''
		for n in list(num):
			str += self._secrets[n]
		return str

	def getOrgInfo(self, name, context=None):
		event_loop = asyncio.get_event_loop()
		result = event_loop.run_until_complete(self.getQuery(name))
		if result != -1:
			soup = BeautifulSoup(result, 'lxml')
			#print(soup.find("div", class_="boss-td").get_text("|", strip=True))
			faren = soup.find("div", class_="humancompany")
			#print(faren.get_text("|", strip=True))
			self._fields["法人"] = self.sub(faren.get_text("|", strip=True))
			#registered capital
			td = faren.find_parent("td")
			tr = faren.find_parent("tr")
			self._fields["注册资本"] = self.plain(td.find_next_sibling().find("text", class_="tyc-num lh24").get_text(strip=True))
		#event_loop.close()
			tr = tr.find_next_sibling()
			self._fields["注册时间"] = self.plain(tr.find("text", class_="tyc-num lh24").get_text(strip=True))	
			tr = tr.find_next_sibling()
			self._fields["公司状态"] = tr.find("div", class_="num-opening").get_text(strip=True)
			#
			print(self._fields)
				

o = OrgServiceI()
o.getOrgInfo("华为技术有限公司")		