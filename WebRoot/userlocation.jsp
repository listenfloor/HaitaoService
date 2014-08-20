<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String x = request.getParameter("x");
String y = request.getParameter("y");
%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">
body, html,#allmap {width: 100%;height: 100%;overflow: hidden;margin:0;}
#l-map{height:100%;width:78%;float:left;border-right:2px solid #bcbcbc;}
#r-result{height:100%;width:20%;float:left;}
</style>
<script type="text/javascript" src="http://api.map.baidu.com/api?v=1.5&ak=25E1eefa0fd2170a2bcb535617f08ad3"></script>
<script type="text/javascript" src="http://developer.baidu.com/map/jsdemo/demo/convertor.js"></script>
<title>GCJ-02转百度</title>
</head>
<body>
<div id="allmap"></div>
</body>
</html>
<script type="text/javascript">
var ggPoint = new BMap.Point(116.562035,40.067467);
var bm = new BMap.Map("allmap");
bm.centerAndZoom(ggPoint, 15);
bm.addControl(new BMap.NavigationControl());

translateCallback = function (point){
var marker = new BMap.Marker(point);
bm.addOverlay(marker);
var label = new BMap.Label("用户提交位置:",{offset:new BMap.Size(20,-10)});
marker.setLabel(label);
bm.setCenter(point);
}

setTimeout(function(){
BMap.Convertor.translate(ggPoint,2,translateCallback);}, 2000);
</script>
