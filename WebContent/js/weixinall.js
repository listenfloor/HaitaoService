var jsoncontent,jsonall="";
var arr = new Array("001","003","005","006","012","014","016","018","020","023","043","045","055","057","064","065","071","072","074","077","079","080","081","083","085","086","089","096","098","105","112","114","117","120","124","125","126","128","129","131","134","137","157","160","163","168","170","172","176","180","182","183","189","203","205","214","216","217","229","232","234","235","239","249","250","255","257","258","262","265","266","272","288","289","297","324","401","403","406","412","421","452","465","479","494","505","512","513","525","539","542","543","555","578","580","592","603","607","615","618","619","629","651","672","673","675","688","695","706","724","731","738","761","774","779","781","783","784","785","800","803","804","807","810","811","822","829","831","843","844","847","851","866","870","871","873","876","880","883","891","893","900","932","933","988","989","996","999","C12","OTH","RFS","SNR");
$(document).ready(function(){
    $.ajax({
        type: "POST",
        async: false,
        url: "/WXReportServlet",
        data: { type: "message" },
        success:function(data) {

            jsoncontent=  $.parseJSON(data);

        },
        error: function() {
            alert("Error");
        }
    })
    $.ajax({
        type: "POST",
        async: false,
        url: "/WXReportServlet",
        data: { type: "userlist" },
        success:queryAll
    })
});
function  jsonAll(data){
    jsonall=  $.parseJSON(data);
}
function queryAll(data){

    jsonAll(data);
    var sender = "system";
    var receiver="system";
    var content="";
    var keyword="";
  //  alert(jsoncontent.length);
    var table ="<table border='1' style='background-color: #09C7F7;'><tr style='background-color: white;'><td align='center' width='90' >日期<td width='80' align='center'>时间</td><td width='80' align='center'>发送者</td><td width='80' align='center'>接收者</td><td width='800'>内容</td><td width='30'>消息是否发送成功</td><td width='30'>关键字</td><td width='30'>小时</td></tr>";

    for(var i=0;i<jsoncontent.length;i++){
        for(var j=0;j<jsonall.length;j++){
            if(jsoncontent[i].sender==jsonall[j].wxfakeid){
                sender=jsonall[j].nickname;
            }
            if(jsoncontent[i].receiver==jsonall[j].wxfakeid){
                receiver=jsonall[j].nickname;
            }
        }
        if(sender==""){
            sender = "system";
        }
        if(receiver==""){
            receiver="system";
        }



        if(jsoncontent[i].content.indexOf("图文消息，消息内容=航空运单轨迹")>=0){
            keyword="答复运单轨迹";
        }
        else  if(jsoncontent[i].content.indexOf("图文消息，消息内容=中外运电商")>=0){
            keyword="答复订单轨迹";
        }
        else  if(jsoncontent[i].content=="图文消息，消息ID=10000065"){
            keyword="答复错误请求";
        }
        else  if(jsoncontent[i].content=="图文消息，消息ID=10000119"){
            keyword="答复帮助";
        }
        else  if(jsoncontent[i].content.indexOf("图文消息，消息内容=TACT运价查询")>=0){
            keyword="答复tact";
        }
        else  if(jsoncontent[i].content.indexOf("欢迎关注指尖货运微信公众账号")>=0){
            keyword="关注";
        }
        else  if(jsoncontent[i].content=="您的请求已收到，请稍等片刻，我们正在处理..."){
            keyword="等待";
        }
        else  if(jsoncontent[i].content.indexOf("您输入的订单号不存在，请确认订单号正确。")>=0){
            keyword="订单错误";
        }
        else  if(jsoncontent[i].content.indexOf("您输入的运单号不存在，请确认运单号正确。")>=0){
            keyword="运单错误";
        }
        else  if(jsoncontent[i].content=="您所查询的运价没有记录。"){
            keyword="无运价";
        }
        else  if(jsoncontent[i].content.toLocaleLowerCase().indexOf("help")>=0 ||jsoncontent[i].content.indexOf("帮助")>=0 ){
            keyword="帮助";
        }
        else  if(jsoncontent[i].content.indexOf('五分钟内回复"y"即可订阅此运单') >=0){
            keyword="订阅提醒";
        }
        else  if(jsoncontent[i].content.indexOf("您查询的运单信息我们正在处理中，请您稍后，这可能需要几十分钟的时间...") >=0){
            keyword="运单无记录";
        }
        else  if(jsoncontent[i].content.toLocaleLowerCase().indexOf("tact") >=0){
            keyword="tact";
        } else{
            keyword="其他";
        }
        var value = jsoncontent[i].content.trim().replace(/[^0-9]/ig,"");//取字符串中的数字
            if(value.substr(0,3)=="010"&&value.length==11){
                keyword="订单";
            }

        for(var t = 0; t<=arr.length;t++){
        if(value.length==11&&value.substr(0,3)==arr[t]){
                    keyword="运单";
        }
        }
        if(jsoncontent[i].content.trim().toLocaleLowerCase()=="y"){
             keyword = "订阅";
        }
        if(jsoncontent[i].content.trim().indexOf('已成功订阅，运单轨迹的变化会及时通知您！')>=0){
            keyword = "答复订阅";
        }

        content+="<tr style='background-color: white;'><td nowrap='true' align='center'>"+jsoncontent[i].datetime.substring(0,10)+"</td><td align='center'>"+jsoncontent[i].datetime.substring(11,19)+"</td><td  align='center'>"+sender+"</td><td  align='center'>"+receiver+"</td><td>"+jsoncontent[i].content+"</td><td>"+jsoncontent[i].success+"</td><td>"+keyword+"</td><td>"+jsoncontent[i].datetime.substring(11,13)+"</td></tr>";
        sender = "";
        receiver="";
        keyword="";
    }

    var htmlcontent = table+content+"</table>";
    // alert(htmlcontent);
    $("#alluser").html(htmlcontent);
}