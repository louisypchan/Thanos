import signal, sys, Ice
import svc, org, re
import urllib.request, urllib.parse
from bs4 import BeautifulSoup
import io, gzip, redis, json

class OrgServiceI(org.OrgService):
	# def __init__(self, name):
	# 	self._name = name
	def __init__(self):
		# redis
		pool = redis.ConnectionPool(host="192.168.10.208", port=45000, decode_responses=True)
		self._redis = redis.Redis(connection_pool=pool)
		self._host = "www.qichacha.com"
		self._url = "https://%s/search?key=%s" % (self._host, "")
		self._referer = "https://%s" % (self._host)
		#TODO: make it automatic
		_session = "lnpi16dbjvd83ftb43atc8lg77"
		_zgDid = "%7B%22did%22%3A%20%22164a6dcfcc71291-09ff3ca9947395-5b193413-1fa400-164a6dcfcc879%22%7D"
		self._cookie = "PHPSESSID=%s;zg_did=%s;" % (_session, _zgDid)
		#get PHPSESSID
		#self.cookie = http.cookiejar.CookieJar()
		#urllib.request.install_opener(urllib.request.build_opener(urllib.request.HTTPCookieProcessor(self.cookie)))
		self._useragent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36"
		self._headers = {
			"Accept" : "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
			"Accept-Encoding" : "gzip",
			"Referer" : self._referer,
			"Cookie" : self._cookie,
			"User-Agent" : self._useragent
		}
		self._symbol = "："
		self._fields = {
			"统一社会信用代码" : "N/A",
			"纳税人识别号" : "N/A",
			"注册号" : "N/A",
			"组织机构代码": "N/A",
			"公司类型" : "N/A",
			"所属行业" : "N/A",
			"登记机关" : "N/A",
			"所属地区" : "N/A",
			"曾用名" : "N/A",
			"核准日期" : "N/A",
			"营业期限" : "N/A",
			"企业地址" : "N/A",
			"法人" : "N/A"
		}

	def UUID(self):
		#intend to implement zg did
		print("1")	

	def getExactlyURL(self, name):
		_name = urllib.parse.quote(name)
		req = urllib.request.Request(url=self._url + _name, 
			headers=self._headers)
		try:
			r = urllib.request.urlopen(req)
		except urllib.error.URLError as e:
			print(e.code, ':', e.reason)
		html = self.gzip(r.read())
		soup = BeautifulSoup(html, 'lxml') #default 
		# get the exactly url
		aTag = soup.find("a", text=name)
		return aTag["href"]

	def grab(self, name, url):
		#print("grab")
		req = urllib.request.Request(url=url, 
			headers=self._headers)
		try:
			r = urllib.request.urlopen(req)
		except urllib.error.URLError as e:
			print(e.code, ':', e.reason)

		html = self.gzip(r.read())
		soup = BeautifulSoup(html, 'lxml') #default 
		#
		bossTd = soup.find("div", class_="boss-td")
		tds = soup.find("table", class_="ntable").find_next_sibling().find_all("td", class_="tb")
		boss = bossTd.find("a", class_="bname")
		self._fields["法人"] = boss.get_text(strip=True)
		for td in tds:
			k = td.get_text().strip().replace(self._symbol, "")
			if self._fields.get(k):
				self._fields[k] = re.sub(r'\|[^\|]+', '', td.find_next_sibling().get_text("|", strip=True))
		#store into redis
		self._redis.set(name, json.dumps(self._fields))

	def gzip(self, data):
		buf = io.BytesIO(data)
		f = gzip.GzipFile(fileobj=buf)
		html = f.read()
		buf.close()
		f.close()
		return html

	def getOrgInfo(self, name, context=None):
		# try to get information from redis
		if self._redis.exists(name):
			self._fields = json.loads(self._redis.get(name))
		else:
			_url = self.getExactlyURL(name)
			self.grab(name, self._referer + _url)
		_org = svc.Org()
		_org.name = name
		_org.juridical = self._fields["法人"]
		_org.uscc = self._fields["统一社会信用代码"]
		_org.tin = self._fields["纳税人识别号"]
		_org.registerNumber = self._fields["注册号"]
		_org.code = self._fields["组织机构代码"]
		_org.term = self._fields["公司类型"]
		_org.category = self._fields["所属行业"]
		_org.ra = self._fields["登记机关"]
		_org.region = self._fields["所属地区"]
		_org.nub = self._fields["曾用名"]
		_org.doa = self._fields["核准日期"]
		_org.scope = self._fields["营业期限"]
		_org.addr = self._fields["企业地址"]
		return _org


#o = OrgServiceI()
#print(o.getOrgInfo("华为技术有限公司"))