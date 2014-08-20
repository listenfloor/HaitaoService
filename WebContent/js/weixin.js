//var jsonlist=[
//    { "openid":"omQjGji2ryvc9pFo2TRWT5RCoU9M", "wxid":"weiweiluke", "wxfakeid":"3660475","nickname":"大萝卜",  "datetime":"2013-04-09 11:30", "optype":"unsubscribe", "subscribedate":"2013-04-04 10:10", "unsubscribedate":"" },
//    { "openid":"omQjGji2ryvc9pFo2TRWT5RCoU9M", "wxid":"weiweiluke", "wxfakeid":"3660475","nickname":"大萝卜", "datetime":"2013-04-07 11:30", "optype":"unsubscribe", "subscribedate":"", "unsubscribedate":"2013-04-09 11:30" },
//    { "openid":"omQjGji2ryvc9pFo2TRWT5RCoU9M", "wxid":"weiweiluke", "wxfakeid":"3660475", "nickname":"大萝卜", "datetime":"2013-04-10 11:30", "optype":"subscribe", "subscribedate":"", "unsubscribedate":"2013-04-09 11:30" }
//];
//var jsoncontent=[{"datetime":"2013-04-09 11:30","sender":"3660475","receiver":"3660476","messagetype":"text","content":"你好"},
//    {"datetime":"2013-04-07 11:30","sender":"3660477","receiver":"3660476","messagetype":"text","content":"你好"},
//    {"datetime":"2013-04-08 11:30","sender":"3660476","receiver":"3660475","messagetype":"text","content":"你好"},
//    {"datetime":"2013-04-10 11:30","sender":"3660476","receiver":"3660477","messagetype":"imageandtext","content":"你好"}];
//var jsonall=[{ "openid":"omQjGji2ryvc9pFo2TRWT5RCoU9M", "wxid":"weiweiluke", "wxfakeid":"3660475","nickname":"大萝卜","userstatus":"subscribed"},
//    { "openid":"omQjGji2ryvc9pFo2TRWT5RCoU9M", "wxid":"weiweiluke", "wxfakeid":"3660476","nickname":"小萝卜","userstatus":"unsubscribed"},
//    { "openid":"omQjGji2ryvc9pFo2TRWT5RCoU9M", "wxid":"weiweiluke", "wxfakeid":"3660477","nickname":"中萝卜","userstatus":"unsubscribed"}
//];
var date = "";
$(document).ready(function(){
    var y,month,day="";
    var d=new Date();
    y=d.getFullYear();
    //alert(y);
    month=d.getMonth()+1;
    if(month<10){
        month="0"+month;
    }
    day=d.getDate();
    if(day<10){
        day="0"+day;
    }
    date=y+month+day;

    $.ajax({
        type: "POST",
        url: "/WeixinService/WXReportServlet",
        data: { type: "subscribe", startdate: date,enddate: date },
        success: function(response) {
            onLoad(response);
        }
    });
    $("#startdate").val(date);
    $("#enddate").val(date);

});
function onLoad(data){
   // $("#today").removeClass("show");
//   alert("!!!!!!!!!!"+data);
   var jsonlist = $.parseJSON(data);
   //alert(jsonlist);

  //  $("#startdate").val(date);
   // $("#enddate").val(date);
	var htmlcontent="<table border='1' style='background-color: #09C7F7;'><tr style='background-color: white;'>"+
					"<td>openid</td>"+
					"<td>wxid</td>"+
					"<td>wxfakeid</td>"+
					"<td>nickname</td>"+
					"<td>datetime</td>"+
					"<td>optype</td>"+
					"<td>subscribedate</td>"+
					"<td>unsubscribedate</td>"+
					"</tr>";
	for(var i=0;i<jsonlist.length;i++){
            if(jsonlist[i].optype=="unsubscribe"){
                jsonlist[i].optype="取消关注";
            }
            if(jsonlist[i].optype=="subscribe"){
                jsonlist[i].optype="关注";
            }
			htmlcontent+="<tr  style='background-color: white;'>" +
					"<td>"+jsonlist[i]["openid"]+"</td>"
					+"<td>"+jsonlist[i].wxid+"</td>"
					+"<td>"+jsonlist[i].wxfakeid+"</td>"
					+"<td>"+jsonlist[i].nickname+"</td>"
					+"<td>"+jsonlist[i].datetime+"</td>"
					+"<td>"+jsonlist[i].optype+"</td>"
					+"<td>"+jsonlist[i].subscribedate+"</td>"
					+"<td>"+jsonlist[i].unsubscribedate+"</td>"
                    +"</tr>";

    }
	htmlcontent+="</table>"
	//alert(htmlcontent);
	$("#today").html(htmlcontent);
   
	
}
function search(){
    $("#today").html("");
    //alert("aaaaa");
   // $("#today").addClass("show");

	var startdate=$("#startdate").val();
    var enddate=$("#enddate").val();
    $.ajax({
        type: "POST",
        url: "/WeixinService/WXReportServlet",
        data: { type: "subscribe", startdate: startdate,enddate: enddate },
        success: function(response) {
            onSearch(response);
        }
    });

   }

function onSearch(alldate){
    var jsonlist = $.parseJSON(alldate);
    var htmlcontent="<table border='1' style='background-color: #09C7F7;'><tr style='background-color: white;'>"+
        "<td>openid</td>"+
        "<td>wxid</td>"+
        "<td>wxfakeid</td>"+
        "<td>nickname</td>"+
        "<td>datetime</td>"+
        "<td>optype</td>"+
        "<td>subscribedate</td>"+
        "<td>unsubscribedate</td>"+
        "</tr>";
    for(var i=0;i<jsonlist.length;i++){

            if(jsonlist[i].optype=="unsubscribe"){
                jsonlist[i].optype="取消关注";
            }
            if(jsonlist[i].optype=="subscribe"){
                jsonlist[i].optype="关注";
            }
            htmlcontent+="<tr style='background-color: white;'>" +
                "<td >"+jsonlist[i]["openid"]+"</td>"
                +"<td>"+jsonlist[i].wxid+"</td>"
                +"<td>"+jsonlist[i].wxfakeid+"</td>"
                +"<td>"+jsonlist[i].nickname+"</td>"
                +"<td>"+jsonlist[i].datetime+"</td>"
                +"<td>"+jsonlist[i].optype+"</td>"
                +"<td>"+jsonlist[i].subscribedate+"</td>"
                +"<td>"+jsonlist[i].unsubscribedate+"</td>" +
                "</tr>";

    }
    htmlcontent+="</table>"
    //alert(htmlcontent);
    $("#today").html(htmlcontent);
}




