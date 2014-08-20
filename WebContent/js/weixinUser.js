//var date = "";
var jsoncontent="";
var jsonall="";
var datetoday="";
var arr = new Array("001","003","005","006","012","014","016","018","020","023","043","045","055","057","064","065","071","072","074","077","079","080","081","083","085","086","089","096","098","105","112","114","117","120","124","125","126","128","129","131","134","137","157","160","163","168","170","172","176","180","182","183","189","203","205","214","216","217","229","232","234","235","239","249","250","255","257","258","262","265","266","272","288","289","297","324","401","403","406","412","421","452","465","479","494","505","512","513","525","539","542","543","555","578","580","592","603","607","615","618","619","629","651","672","673","675","688","695","706","724","731","738","761","774","779","781","783","784","785","800","803","804","807","810","811","822","829","831","843","844","847","851","866","870","871","873","876","880","883","891","893","900","932","933","988","989","996","999","C12","OTH","RFS","SNR");
var y,month,day,start,end="";
var d=new Date();
y=d.getFullYear();
month=d.getMonth()+1;
//hours= d.getHours();
//minutes= d.getMinutes();
//if(hours<10){
//    hours="0"+hours;
//}
//if(minutes<10){
//    minutes="0"+minutes;
//}
if(month<10){
    month="0"+month;
}
day=d.getDate();
if(day<10){
    day="0"+day;
}
//date=y+month+day;
start=y+"-"+month+"-"+day+" "+"00:00";
end=y+"-"+month+"-"+day+" "+"23:59";
$(document).ready(function(){
    // alert(date);
    $.ajax({
        type: "POST",
        async: false,
        url: "/WeixinService/WXReportServlet",
        data: { type: "userlist" },
        success:jsonAll
    });
    $.ajax({
        type: "POST",
        url: "/WeixinService/WXReportServlet",
        data: {  type: "message", startdate:start,enddate:end},
        success:onLoaduser
    });
});

//function  jsonContent(data){
//
//    jsoncontent=  $.parseJSON(data);
//    return jsoncontent;
//}
function  jsonAll(data){
    jsonall =  $.parseJSON(data);
   // alert(jsonall);
    return jsonall;
}
function jsonToday(data){
    var jsontoday =  $.parseJSON(data);
    return jsontoday;
}
function onLoaduser(data){
   // var jsoncontent1=jsoncontent;
    var jsonall1=jsonall;
    var jsontoday=jsonToday(data);
    //alert(jsontoday);
    var sender="system";
    var receiver="system";
    $("#startdate").val(start);
    $("#enddate").val(end);
    var userlist="用户列表<br/><br/>";
    var user="";
    userlist+="<a href='#' onclick=queryUser('all'); style='cursor:hand ;text-decoration: none;'>所有用户</a><br/>";
    for(var i=0;i<jsonall.length;i++){
        user+="<a href='#'  onclick=queryUser('"+jsonall[i].wxfakeid+"'); style='cursor:hand ;text-decoration: none;'>"+jsonall[i].nickname+"</a><br/>";
    }
    userlist+=user;
    $("#userlist").html(userlist);
    var content="";
    var keyword="";
    var table ="<table border='1' style='background-color: #09C7F7;'><tr style='background-color: white;'><td align='center' width='90' >日期<td width='80' align='center'>时间</td><td width='80' align='center'>发送者</td><td width='80' align='center'>接收者</td><td width='800'>内容</td><td width='30'>消息是否发送成功</td><td>关键字</td><td width='30'>小时</td><td>微信消息ID</td></tr>";
    for(var i=0;i<jsontoday.length;i++){

            for(var j=0;j<jsonall.length;j++){
                if(jsontoday[i].sender==jsonall[j].wxfakeid){
                    sender=jsonall[j].nickname;
                }
                if(jsontoday[i].receiver==jsonall[j].wxfakeid){
                    receiver=jsonall[j].nickname;
                }
            }
            if(sender=="" || sender=="undefined"){
                sender = "system";
            }
            if(receiver=="" || receiver=="undefined"){
                receiver="system";
            }
             if(jsontoday[i].content.indexOf("图文消息，消息内容=航空运单轨迹")>=0){
                 keyword="答复运单轨迹";
               }
                else  if(jsontoday[i].content.indexOf("图文消息，消息内容=中外运电商")>=0){
                keyword="答复订单轨迹";
              }
                else  if(jsontoday[i].content=="图文消息，消息ID=10000065"){
                    keyword="答复错误请求";
                }
             else  if(jsontoday[i].content=="图文消息，消息ID=10000119"){
                 keyword="答复帮助";
             }
             else  if(jsontoday[i].content.indexOf("图文消息，消息内容=TACT运价查询")>=0){
                 keyword="答复tact";
             }
             else  if(jsontoday[i].content.indexOf("欢迎关注指尖货运微信公众账号")>=0){
                 keyword="关注";
             }
             else  if(jsontoday[i].content=="您的请求已收到，请稍等片刻，我们正在处理..."){
                 keyword="等待";
             }
             else  if(jsontoday[i].content.indexOf("您输入的订单号不存在，请确认订单号正确。")>=0){
                 keyword="订单错误";
             }
             else  if(jsontoday[i].content.indexOf("您输入的运单号不存在，请确认运单号正确。")>=0){
                 keyword="运单错误";
             }
             else  if(jsontoday[i].content=="您所查询的运价没有记录。"){
                 keyword="无运价";
             }
             else  if(jsontoday[i].content.toLocaleLowerCase().indexOf("help")>=0 ||jsontoday[i].content.indexOf("帮助") >=0){
                 keyword="帮助";
             }
             else  if(jsontoday[i].content.indexOf('五分钟内回复"y"即可订阅此运单') >=0){
                 keyword="订阅提醒";
             }
             else  if(jsontoday[i].content.indexOf("您查询的运单信息我们正在处理中，请您稍后，这可能需要几十分钟的时间...") >=0){
                 keyword="运单无记录";
             }
             else  if(jsontoday[i].content.toLocaleLowerCase().indexOf("tact")>=0 ){
                 keyword="tact";
             } else{
                 keyword="其他";
                 try{
 	                if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("image")>=0){
 	                	keyword +="接收图片信息";
 	                }else if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("voice")>=0){
 	                	keyword +="接收音频信息";
 	                }else if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("location")>=0){
 	                	keyword +="接收坐标信息";
 	                }
                 }catch(e){}
             }
        var value = jsontoday[i].content.trim().replace(/[^0-9]/ig,"");//取字符串中的数字
        if(value.substr(0,3)=="010"&&value.length==11){
            keyword="订单";
        }
        for(var t = 0; t<=arr.length;t++){
            if(value.length==11&&value.substr(0,3)==arr[t]){
                keyword="运单";
            }
        }
        if(jsontoday[i].content.trim().toLocaleLowerCase()=="y"){
            keyword = "订阅";
        }
        if(jsontoday[i].content.trim().indexOf('五分钟内回复"y"即可订阅此运单')>=0){
            keyword = "答复订阅";
        }
        content+="<tr style='background-color: white;'><td nowrap='true' align='center'>"+jsontoday[i].datetime.substring(0,10)+"</td><td align='center'>"+jsontoday[i].datetime.substring(11,19)+"</td><td  align='center'>"+sender+"</td><td  align='center'>"+receiver+"</td><td>"+jsontoday[i].content+"</td><td>"+jsontoday[i].success+"</td><td>"+keyword+"</td><td>"+jsontoday[i].datetime.substring(11,13)+"</td><td>"+jsontoday[i].wxmsgid+"</td></tr>";
             keyword="";
             sender = "";
            receiver="";
    }
    var htmlcontent = table+content+"</table>";
    $("#content").html(htmlcontent);
}

function searchUser(){
    var startdate=$("#startdate").val();
    var enddate=$("#enddate").val();

    $("#content").html("");
    var table ="<table border='1' style='background-color: #09C7F7;'><tr style='background-color: white;'><td align='center' width='90' >日期<td width='80' align='center'>时间</td><td width='80' align='center'>发送者</td><td width='80' align='center'>接收者</td><td width='800'>内容</td><td width='30'>消息是否发送成功</td><td>关键字</td><td width='30'>小时</td><td>微信消息ID</td></tr>";

    var content="";
    var sender = "system";
    var  receiver= "system";
    $.ajax({
        type: "POST",
        async: false,
        url: "/WeixinService/WXReportServlet",
        data: { type: "message", startdate: startdate,enddate:enddate },
        success:function(data) {
            jsoncontent =  $.parseJSON(data);
        },
        error: function() {
            alert("Error");
        }
    });
    //alert(jsoncontent);
    var keyword="";
    for (var i=0;i<jsoncontent.length;i++){
        var sdate = jsoncontent[i].datetime;
        if(startdate<=sdate && enddate>=sdate){
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
            else  if(jsoncontent[i].content.toLocaleLowerCase().indexOf("tact")>=0 ){
                keyword="tact";
            } else{
                keyword="其他:";
                try{
	                if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("image")>=0){
	                	keyword +="接收图片信息";
	                }else if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("voice")>=0){
	                	keyword +="接收音频信息";
	                }else if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("location")>=0){
	                	keyword +="接收坐标信息";
	                }
                }catch(e){}
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
            if(jsoncontent[i].content.trim().indexOf('五分钟内回复"y"即可订阅此运单')>=0){
                keyword = "答复订阅";
            }
            content+="<tr style='background-color: white;'><td nowrap='true' align='center'>"+jsoncontent[i].datetime.substring(0,10)+"</td><td align='center'>"+jsoncontent[i].datetime.substring(11,19)+"</td><td  align='center'>"+sender+"</td><td  align='center'>"+receiver+"</td><td>"+jsoncontent[i].content+"</td><td>"+jsoncontent[i].success+"</td><td>"+keyword+"</td><td>"+jsoncontent[i].datetime.substring(11,13)+"</td><td>"+jsoncontent[i].wxmsgid+"</td></tr>";
            sender = "";
            receiver="";
            keyword="";
        }
    }
    var htmlcontent=table+content+"</table>";

    $("#content").html(htmlcontent);
}
function queryUser(user){

//    var jsoncontent=jsonContent();
//    var jsonall=jsonAll();

    $("#content").html("");
    var sender = "system";
    var receiver = "system";
        var content="";
    var table ="<table border='1' style='background-color: #09C7F7;'><tr style='background-color: white;'><td align='center' width='90' >日期<td width='80' align='center'>时间</td><td width='80' align='center'>发送者</td><td width='80' align='center'>接收者</td><td width='800'>内容</td><td width='30'>消息是否发送成功</td><td>关键字</td><td width='30'>小时</td><td>微信消息ID</td></tr>";
    var startdate=$("#startdate").val();
    var enddate=$("#enddate").val();


    if(user=="all"){
        $.ajax({
            type: "POST",
            async: false,
            url: "/WeixinService/WXReportServlet",
            data: { type: "message", startdate: startdate,enddate:enddate },
            success:function(data) {
                jsoncontent=  $.parseJSON(data);
            },
            error: function() {
                alert("Error");
            }
        });
        var keyword="";
        for(var i=0;i<jsoncontent.length;i++){
            var sdate = jsoncontent[i].datetime;

            if(sdate>=startdate && sdate<=enddate){

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
                else  if(jsoncontent[i].content.toLocaleLowerCase().indexOf("tact")>=0 ){
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
                if(jsoncontent[i].content.trim().indexOf('五分钟内回复"y"即可订阅此运单')>=0){
                    keyword = "答复订阅";
                }
                content+="<tr style='background-color: white;'><td nowrap='true' align='center'>"+jsoncontent[i].datetime.substring(0,10)+"</td><td align='center'>"+jsoncontent[i].datetime.substring(11,19)+"</td><td  align='center'>"+sender+"</td><td  align='center'>"+receiver+"</td><td>"+jsoncontent[i].content+"</td><td>"+jsoncontent[i].success+"</td><td>"+keyword+"</td><td>"+jsoncontent[i].datetime.substring(11,13)+"</td><td>"+jsoncontent[i].wxmsgid+"</td></tr>";
                sender = "";
                receiver="";
                keyword="";
            }
        }
        var htmlcontent=table+content+"</table>";
        $("#content").html(htmlcontent);
    } else{
       // alert("aaaaaaaaaa");
        $.ajax({
            type: "POST",
            async: false,
            url: "/WeixinService/WXReportServlet",
            data: { type: "message", startdate: startdate,enddate:enddate,wxfakeid:user },
            success:function(data) {
                jsoncontent=  $.parseJSON(data);
            },
            error: function() {
                alert("Error");
            }
        });
        var keyword="";
       // alert(jsoncontent.length);
        for(var i=0;i<jsoncontent.length;i++){
            var sdate = jsoncontent[i].datetime;
            if(sdate>=startdate &&sdate<=enddate){
                if(user==jsoncontent[i].sender || user==jsoncontent[i].receiver){
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
                    else  if(jsoncontent[i].content.toLocaleLowerCase().indexOf("help") >=0||jsoncontent[i].content.indexOf("帮助")>=0 ){
                        keyword="帮助";
                    }
                    else  if(jsoncontent[i].content.indexOf('五分钟内回复"y"即可订阅此运单') >=0){
                        keyword="订阅提醒";
                    }
                    else  if(jsoncontent[i].content.indexOf("您查询的运单信息我们正在处理中，请您稍后，这可能需要几十分钟的时间...") >=0){
                        keyword="运单无记录";
                    }
                    else  if(jsoncontent[i].content.toLocaleLowerCase().indexOf("tact")>=0 ){
                        keyword="tact";
                    } else{
                        keyword="其他";
                        try{
        	                if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("image")>=0){
        	                	keyword +="接收图片信息";
        	                }else if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("voice")>=0){
        	                	keyword +="接收音频信息";
        	                }else if(jsoncontent[i].messagetype.toLocaleLowerCase().indexOf("location")>=0){
        	                	keyword +="接收坐标信息";
        	                }
                        }catch(e){}
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
                    if(jsoncontent[i].content.trim().indexOf('五分钟内回复"y"即可订阅此运单')>=0){
                        keyword = "答复订阅";
                    }
                    content+="<tr style='background-color: white;'><td nowrap='true' align='center'>"+jsoncontent[i].datetime.substring(0,10)+"</td><td align='center'>"+jsoncontent[i].datetime.substring(11,19)+"</td><td  align='center'>"+sender+"</td><td  align='center'>"+receiver+"</td><td>"+jsoncontent[i].content+"</td><td>"+jsoncontent[i].success+"</td><td>"+keyword+"</td><td>"+jsoncontent[i].datetime.substring(11,13)+"</td><td>"+jsoncontent[i].wxmsgid+"</td></tr>";
                    sender = "";
                    receiver="";
                    keyword="";
                }
            }
        }
        var htmlcontent=table+content+"</table>";
        $("#content").html(htmlcontent);
    }
}

