/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var findUserRest = "/services/user/getUser/";
var createChat = "/services/chat/createChat";

function connect() {
    var username = currUser.name;
    ws = new WebSocket("ws://" + document.location.host + "/ChatEndpoint/" + username);


    ws.onmessage = function (event) {
        var log = document.getElementById("log");
        console.log(event.data);
        var message = JSON.parse(event.data);
        log.innerHTML += message.from + " : " + message.content + "\n";
    };
}

function initChat() {
    if (currUser == null) {
        var log = document.getElementById("log");
        log.innerHTML += "Please login"
    } else {
        connect();
    }
}


function send() {
    var content = document.getElementById("msg").value;
    var json = JSON.stringify({
        "content": content
    });

    ws.send(json);
}

function findUser() {
    var name = $("#addUser").val();
    $.ajax({
        type: "GET",
        url: findUserRest + name,
        datatype: "json",
        async: true,
        success: function (data) {
            $("#foundedUser").empty();
            if (data.user == "Was not found") {
                $("#foundedUser").append("Nobody was found");
            } else {
                $("#foundedUser").append(data[name]);
            }
        }
    });
}

function addChat(){
    $.ajax({
        type:"POST",
        url: createChat,
        datatype: "json",
        
        
    })
}