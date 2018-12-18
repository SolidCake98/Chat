/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var serviceLocation = "ws://" + document.location.host + "/SingleChat";
var chatURL = "/services/chat";
var chat20Message = "/services/chat/messageList";
var countMessageForChats = [];
var countMessageForChats = [];
var prevScrollHForChats = [];
var nameList = [];

var websocket = new WebSocket(serviceLocation);
websocket.onmessage = onMessageReceived;
websocket.onopen = onOpen;
websocket.onerror = onErrorWebSocket;

var currentRoom;
var currentChat;
var chatList;
var chatId = [];
var path;
var myStatus;


function onMessageReceived(message) {
    var control = "";
    var jsonData = JSON.parse(message.data);
    if (jsonData.type == "createChat" && jsonData.tou == currUser) {
        resetChatList();
        return;
    }
    if (jsonData.type == "createGroupChat" && jsonData.tou == currUser) {
        resetChatList();
        //return;
    }
    if (jsonData.type == "text" && jsonData.text !== null && chatId.indexOf(jsonData.chat) != -1 && jsonData.chat == currentRoom && (jsonData.tou != -1 || jsonData.fromu.id == currUser.id)) {
        var messageText = jsonData.text;

        control = '<li class="btnLong class="width-100"">' +
                '<div class="msj macro">' +
                '<div class="text text-l">' +
                '<div class="avatar"><img class="profile-pic" src="' + jsonData.fromu.avatarPath + '" /><small>' + jsonData.fromu.name + '</small></div>' +
                '<p class="width-100 margin-left">' + emojione.toImage(messageText) + '</p>' +
                '</div>' +
                '</div>' +
                '</li>';
        $("#listMessage" + currentRoom).append(control);

        $("#chatMessages" + currentRoom).scrollTop($("#chatMessages" + currentRoom)[0].scrollHeight);

    }

    if (jsonData.type == "file" && chatId.indexOf(jsonData.chat) != -1 && jsonData.chat == currentRoom && jsonData.tou != -1) {
        var messageText = jsonData.text;

        control = '<li class="btnLong class="width-100"">' +
                '<div class="msj macro">' +
                '<div class="text text-l">' +
                '<div class="avatar"><img class="profile-pic" src="' + jsonData.fromu.avatarPath + '" /><small>' + jsonData.fromu.name + '</small></div>' +
                '<p class="width-100 margin-left">' + '<a download="" href="' + jsonData.filePath.path + '">' + jsonData.filePath.title + '</a>' + '</p>' +
                '</div>' +
                '</div>' +
                '</li>';
        $("#listMessage" + currentRoom).append(control);
        $("#chatMessages" + currentRoom).scrollTop($("#chatMessages" + currentRoom)[0].scrollHeight);
    }
    if (jsonData.type == "add") {
        if (jsonData.peer != null && jsonData.tou == currUser.id) {

            resetChatList();
            return;
        }
        if (jsonData.tou == 0 && jsonData.fromu.id != currUser.id) {
            return;
        }
        if (jsonData.peer != null && jsonData.chat != currentRoom) {
            return;
        }
        var messageText = jsonData.text;
        control = '<li class="btnLong class="width-100"">' +
                '<div class="msj macro">' +
                '<div class="text text-l">' +
                '<div class="avatar"><img class="profile-pic" src="' + jsonData.fromu.avatarPath + '" /><small>' + jsonData.fromu.name + '</small></div>' +
                '<p class="width-100 margin-left">' + emojione.toImage(messageText) + '</p>' +
                '</div>' +
                '</div>' +
                '</li>';
        $("#listMessage" + currentRoom).append(control);
        $("#chatMessages" + currentRoom).scrollTop($("#chatMessages" + currentRoom)[0].scrollHeight);
    }
}

function createGroupChat() {
    var groupChatJSON = {chatName: "", nameList: nameList};
    $.ajax({
        type: "POST",
        url: "/services/chat/addGroupChat",
        contentType: 'application/json',
        datatype: "json",
        data: JSON.stringify(groupChatJSON),
        success: function () {
            hideAddGroupChat();
        }
    });
}

function hideAddGroupChat() {
    $(".addGroupChat").toggleClass("display", false);
    sendMessage('createGroupChat', currUser.id, 0);
    resetChatList();
}

function addNameToList() {
    var name = $("#nameGroup").val();
    if (name != null && userIsExist(name)) {
        nameList.push(name);
        $("#usersToAddToGroupChat").append(name + "</br>");
    }
}

function sendMessage(type, user, avatar, chat, textMs) {
    var d;
    if (type == "createChat") {
        textMessage = $("#nameUserToAdd").val();
        var message = JSON.stringify({
            "type": "createChat",
            "fromu": user,
            "tou": 0,
            "text": textMessage,
            "token": getCookie("token"),
            "chat": chat,
        });
        $.ajax({
            type: "GET",
            url: "/services/chat/addChat/" + textMessage,
            datatype: "json",
            success: function (data) {
                sendText(message);
                resetChatList();
                alert("Чат был добавлен");
            }
        });
    }
    if (type == "createGroupChat") {
        var message = JSON.stringify({
            "type": "createGroupChat",
            "fromu": currUser,
            "tou": 0,
            "text": nameList,
            "token": getCookie("token"),
            "chat": chat,
        });
        sendText(message);
        resetChatList();
    }
    if (type == "text" && textMs.val() != "") {
        var message = JSON.stringify({
            "type": "text",
            "fromu": currUser,
            "tou": 0,
            "chat": chat,
            "text": textMs.val().replace(/'/g, "\′"),
            "token": getCookie("token"),
            "avatar": avatar
        });
        sendText(message);
        textMessage.value = "";
    }
    if (type == "file") {
        var message = JSON.stringify({
            "type": "file",
            "fromu": currUser,
            "tou": 0,
            "chat": chat,
            "text": textMs.val().replace(/'/g, "\′"),
            "filePath": path,
            "token": getCookie("token"),
            "avatar": avatar
        });
        sendText(message);
        textMessage.value = "";
    }
    if (type == "add") {
        textMessage = $("#nameUserToAdd").val();
        var message = JSON.stringify({
            "type": "add",
            "fromu": currUser,
            "tou": 0,
            "text": textMessage,
            "token": getCookie("token"),
            "chat": chat,
        });
        sendText(message);
        resetChatList();
    }
    if (type == "ban") {

        var message = JSON.stringify({
            "type": "ban",
            "fromu": currUser,
            "tou": 0,
            "text": user,
            "token": getCookie("token"),
            "chat": chat,
        });
        sendText(message);
    }
    if (type == "moder") {
        var message = JSON.stringify({
            "type": "moder",
            "fromu": currUser,
            "tou": 0,
            "text": user,
            "token": getCookie("token"),
            "chat": chat,
        });
        sendText(message);
    }
}

function connectToChatServer(room) {
    currentRoom = room;
}

function connectAndDrawChat(room) {
    for (var i = 0; i < chatList.length; i++) {
        if (chatList[i].chat.id === room) {
            curChat = chatList[i];
            break;
        }
    }
    $("#listMessage").empty();
    if (currentRoom == undefined) {
        $("#chatMessages").attr("id", "chatMessages" + room);
    } else {
        $("#chatMessages" + currentRoom).attr("id", "chatMessages" + room);
    }

    currentRoom = room;
    var html = '<div id = "listMessage' + room + '" class="width-100">';



    $("#listMessage").empty();
    $("#listMessage").append(html);
    html = "<div class='min-height'><textarea id='textMessage'></textarea></div>";
    html += "<div class=''><button class='button-add' onClick = 'sendMessageWrap(" + room + ")'>Отправить  <i class='fab fa-telegram-plane'></i></button>" +
            "<button class='button-add' data-toggle='modal'  data-target='#pickFileModal'><i class='fas fa-file-upload'></i></button></div></div></div>";
    $("#post").empty();
    $("#post").append(html);
    getUserList(room);
    writeFirst20Message(curChat.chat.id);
    $("#textMessage").emojioneArea({
        tones: false,
        filtersPosition: "bottom",
        search: false,
    });
}

function sendMessageWrap(room) {
    sendMessage('text', currUser.id, currUser.avatarPath, room, $("#textMessage"));
    $(".emojionearea-editor").text("");
}

function sendFileWrap(room) {
    saveFileOnServer(function () {
        sendMessage('file', currUser.id, currUser.avatarPath, room, $("#textMessage"));
    });
}

function banWrap(user, room) {
    sendMessage('ban', user, currUser.avatarPath, room);
}

function moderWrap(user, room) {
    sendMessage('moder', user, currUser.avatarPath, room);
}


function resetChatList() {
    var html = "";
    var chatContainerPage = $("#listRoomPage");
    chatContainerPage.empty();
    $.ajax({
        type: "GET",
        url: chatURL + "/chatList",
        async: false,
        datatype: "json",
        success: function (chatListFromServer) {
            chatList = chatListFromServer;
            if (chatListFromServer === null || chatListFromServer.length <= 0) {
                $("#listRoom").remove();
            } else {
                for (var i = 0; i < chatListFromServer.length; i++) {
                    var tempChat = chatListFromServer[i];
                    html += '<li id="' + tempChat.chat.id + '" class="margin-t list padding">';
                    if (tempChat.userNames.length <= 2) {
                        html += "<div>"
                        html += '<div id="dialog_' + tempChat.chat.id + '" onclick="connectAndDrawChat(' + tempChat.chat.id + ')" class="clickDialog verticalText pointer headerDialog">';
                        html += tempChat.chat.name;
                        html += '</div>';
                        html += '</div>';
                        html += '</li>';
                        html += '<button type="button" class="btn btn-info float-r" data-toggle="modal" data-target="#listUserChat" onclick="getUserList(' + tempChat.chat.id + ')"><i class="fas fa-eye"></i></button>'
                    }
                }
            }
            chatContainerPage.append(html);
            chatList.map(function (o) {
                chatId.push(o.chat.id);
            })
        }
    });
}

function writeMessage(user, avatar, text, room, idListMessage, userName, id) {
    var control = "";
    if (text !== null) {

        control = '<li class="btnLong width-100" id="message' + id + '">' +
                '<div class="msj-rta macro width-100">' +
                '<div class="text text-r width-100">' +
                '<div class="avatar"><img class="profile-pic" src="' + avatar + '" /><small>' + userName + '</small><span class="float-r" onclick="removeMessage(' + id + ')">&times</span></div>' +
                '<p class="width-100 margin-left">' + emojione.toImage(text) + '</p>' +
                '</div>' +
                '</div>' +
                '</li>';
    }
    $("#" + idListMessage).prepend(control);
}


function scroll(room, idChatMessages, idListMessage) {
    wrapper = document.getElementById(idChatMessages);
    prevScrollHForChats[room] = wrapper.scrollHeight;

    wrapper.onscroll = function () {
        var $that = $(this);
        if ($that.scrollTop() <= 0 && prevScrollHForChats[room]) {
            countMessageForChats[room] = countMessageForChats[room] + 20;
            $.ajax({
                type: "GET",
                url: chat20Message + "/" + room + "/" + countMessageForChats[room],
                datatype: "json",
                async: false,
                success: function (data) {
                    for (var i = 0; i < data.length; i++) {
                        writeMessage(data[data.length - 1 - i ].fromUserId, data[data.length - 1 - i ].fromUserPhotoPath, data[data.length - 1 - i].text, room, idListMessage, data[data.length - 1 - i ].fromUserName, data[data.length - 1 - i ].id);
                    }
                    $that.scrollTop($that.prop('scrollHeight') - prevScrollHForChats[room]);
                    prevScrollHForChats[room] = $that.prop('scrollHeight');
                }
            });
        }
    };
}

function writeFirst20Message(room) {
    countMessageForChats[room] = 0;
    //countMessage = 0;
    if (room != null && room != 0) {
        $.ajax({
            type: "GET",
            url: chat20Message + "/" + room + "/" + countMessageForChats[room],
            datatype: "json",
            async: false,
            success: function (data) {
                for (var i = 0; i < data.length; i++) {
                    writeMessage(data[data.length - 1 - i].fromUserId, data[data.length - 1 - i].fromUserPhotoPath, data[data.length - 1 - i].text, room, 'listMessage' + room, data[data.length - 1 - i ].fromUserName, data[data.length - 1 - i ].id);
                }
                var ob = $("#chatMessages" + room);
                var height = ob.prop('scrollHeight');
                scroll(room, "chatMessages" + room, 'listMessage' + room);
                ob.scrollTop(height);
            }
        }
        );


    }
}

function addToChat() {
    if (currentRoom == undefined) {
        alert("Выберите чат");
        return;
    }
    sendMessage('add', currUser.id, currUser.avatarPath, currentRoom);
    $("#nameUserToAdd").val("");
}





function sendText(json) {
    websocket.send(json);
}


function onOpen() {
}
;

function onErrorWebSocket(evt) {
    onError(evt);
}
;

function generateModerPanel(name, id) {
    var ban = '<button class="btn btn-info margin-left" data-dismiss="modal" onclick="banWrap(' + name + ',' + id + ')">Забанить</button>';
    var moder = '<button class="btn btn-info margin-right float-r" data-dismiss="modal" onclick="moderWrap(' + name + ',' + id + ')">Дать модерку</button>';
    var html = ban + moder;
    $("#moder").empty();
    $("#moder").append(html);
}

function moder(name, id) {
    return moderPanel = '<button class="btn btn-info float-r" data-toggle="modal" data-target="#moderPanel" onclick="generateModerPanel(' + name + ',' + id + ')">Панель модерации</button>';
}

function removeMessage(a) {
    if (myStatus.id == 1 || myStatus.id == 5) {
        var s = $("#message" + a);
        s.remove();
        $.ajax({
            type: "DELETE",
            url: chatURL + "/deleteMessage/" + a,
            datatype: "json",
            async: false,
        });
    }
}


function getUserList(chatId) {
    $.ajax({
        type: "GET",
        url: chatURL + "/getUserList/" + chatId,
        datatype: "json",
        async: false,
        success: function (data) {
            $("#listUser").empty();
            for (i in data) {
                if (data[i].userT.id == currUser.id) {
                    myStatus = data[i].status;
                    break;
                }
            }
            if (myStatus.id == 1 || myStatus.id == 5) {
                for (i in data) {
                    if (data[i].userT.id == currUser.id) {
                        var html = '<div class="margin-t-30">' + data[i].userT.name + " " + data[i].status.nameStatus + '</div>';
                    } else {
                        var html = '<div class="margin-t-30">' + data[i].userT.name + " " + data[i].status.nameStatus + moder(data[i].userT.id, data[i].chat.id) + '</div>';
                    }
                    $("#listUser").append(html);
                }
            } else {
                for (i in data) {
                    var html = '<div class="margin-t-30">' + data[i].userT.name + " " + data[i].status.nameStatus + '</div>';
                    $("#listUser").append(html);
                }
            }
        }
    }
    );
}