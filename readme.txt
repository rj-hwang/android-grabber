网络图片抓取器

一) 抓取记录
1) 记录文件：[sdcard]/grabber/records.log

2) 文件内容的格式：(每行一条记录)
[序号]-[抓取时间yyyyMMddHHmmssSSSS] [抓取的URL地址]

二) 抓取配置
1) 配置文件：[sdcard]/grabber/grabber.config.xml
2) 文件内容格式：(每行一条记录)
<grabber>
	<userAgent>Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.79 Safari/535.11</userAgent>
	<items>
		<!-- 分页抓取范例1 -->
		<item id="item01">
			<type>Multi</type>
			<name>妹子图</name>
			<dir>妹子图</dir>
			<url>http://jandan.net/ooxx</url>
			<selector>ol.commentlist>li>p>img</selector>
			<deep>true</deep>
			<pagingUrl>{url}/page-{page}</pagingUrl>
			<pagingSelector>span.current-comment-page</pagingSelector>
			<pagingRegx>(?<=\\[)(\\d)+(?=\\])</pagingRegx>
		</item>
		<!-- 分页抓取范例2 -->
		<item id="item02">
			<type>Multi</type>
			<name>冬日里的女人</name>
			<dir>美女门</dir>
			<url>http://www.mmeinv.com/post/wangqiuzi/8.html</url>
			<selector>div.pic>a>img</selector>
			<deep>true</deep>
			<pagingUrl>{urlName}_{page}.html</pagingUrl>
			<pagingSelector>ul.pagelist>li>a</pagingSelector>
			<pagingRegx>(?<=共)(\\d)+(?=页)</pagingRegx>
		</item>
		
		<!-- 导航后再分页抓取范例 -->
		<item id="nav01">
			<type>Nav</type>
			<name>美女门.今日美女拍行榜</name>
			<dir>美女门</dir>
			<url>http://www.mmeinv.com</url>
			<selector>div.content>ul>li>a</selector>
			<itemId>item4nav01</itemId>
			<item id="item4nav01" nested="true">
				<type>Multi</type>
				<name>{title}</name>
				<dir>{parent}/{title}</dir>
				<url>{href}</url>
				<selector>div.pic>a>img</selector>
				<deep>true</deep>
				<pagingUrl>{urlName}_{page}.html</pagingUrl>
				<pagingSelector>ul.pagelist>li>a</pagingSelector>
				<pagingRegx>(?<=共)(\\d)+(?=页)</pagingRegx>
			</item>
		</item>
	</items>
</grabber>