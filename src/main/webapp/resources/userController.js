/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var service = "services/user";
var userRestLogin = "/services/user/getToken";
var setAvatar = "/services/user/setAvatar";

function initHeader() {
    if (currUser != null) {
        $("#navbar").empty();
        var html = "<li class='nav-item active avatar' id='avatar'>" +
                '<div class="avatar"><img class="profile-pic" src="' + currUser.avatarPath + '" /></div>' +
                "<li class='nav-item active'>" +
                '<div class="dropdown open">' +
                "<a href='' class='btn nav-link dropdown-toggle dropdown-toggle-split' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false' class='btn nav-link'>" + currUser.name + " </a>" +
                '<div class="dropdown-menu">' +
                '<a class="dropdown-item" data-toggle="modal" data-target="#changePictureModal" href="">' +
                'Change picture' +
                '</a>' +
                '</div>' +
                "</li>" +
                "<li class='nav-item active'>" +
                "<a href='' class='btn nav-link' data-toggle='modal' data-target='#signUP' onclick = 'logout()'> Log Out </a>" +
                "</li>";
        $("#signIN").remove();
        $("#signUP").remove();
        $("#navbar").append(html);

    } else {
        var html = "<li class='nav-item active' id='signINButton'>" +
                "<a href='' data-toggle='modal' data-target='#signIN' class='btn nav-link'>Sign In </a>" +
                "</li>" +
                "<li class='nav-item active' id='signUPButton'>" +
                "<a href='' class='btn nav-link' data-toggle='modal' data-target='#signUP'> Sign Up </a>" +
                "</li>";
        $("#navbar").append(html);
    }
}

function reqRegister() {
    let jsonObj = {};
    jsonObj.password = $("#password").val();
    jsonObj.name = $("#username").val();
    jsonObj.email = $("#email").val();
    jsonObj.role = "1";
    if (jsonObj.password == "") {
        alert("Enter password");
        return;
    }
    if (jsonObj.name == "") {
        alert("Enter name");
        return;
    }
    var r = /^\w+@\w+\.\w{2,4}$/i;
    if (!r.test(jsonObj.email)) {
        alert("Введите корректный e-mail");
        return;
    }

    //console.log(jsonObj);
    var result
    $.ajax({
        type: "POST",
        url: service,
        dataType: 'json',
        contentType: 'application/json',
        async: false,
        data: JSON.stringify(jsonObj),
        success: function (datas) {
            result = datas[jsonObj.name];
        },

    });
    alert(result);
}

function login() {
    let jsonObj = {};
    jsonObj.password = $("#passwordA").val();
    jsonObj.name = $("#usernameA").val();
    $.ajax({
        type: "GET",
        url: userRestLogin + "/" + jsonObj.name + "/" + jsonObj.password,
        datatype: "json",
        async: true,
        success: function (jsondata) {
            if (jsondata.msg != null) {
                //writeErrorMessage(jsondata.msg);
            } else {
                document.cookie = "token=" + jsondata.token;
                location.reload();
            }
        },
        error: function () {

        }
    });
}

function logout() {
    deleteCookie("token");
    location.reload();
}

function uploadAvatarForCurrent(res) {
    if (res != "") {
        $.ajax({
            type: "PUT",
            url: setAvatar,
            datatype: "json",
            contentType: 'application/json',
            data: JSON.stringify(res),
            success: function (data, textStatus, jqXHR) {
                alert("Avatar updated successfully");
                $("#avatar").attr("src", res.filePath);

            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(textStatus);
            }
        });
    }
}

function userIsExist(name) {
    var is
    $.ajax({
        type: "GET",
        url: service + "/getUser/" + name,
        datatype: "json",
        async: false,
        success: function (data) {
            if (data[name] == "Was found") {
                is = true;
            } else {
                is = false;
            }
        },
        error: function (data) {

        }
    });
    return is;
}

