/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var saveFile = "services/file/saveFile";
var selectedArea;
var imgWidth;
var imgHeight;
var imgScale;


function saveAvatarOnServer() {
    var reader = new FileReader();
    reader.readAsDataURL($('#file')[0].files[0]);
    reader.onloadend = function () {
        var base64data = reader.result;
        var message = JSON.stringify({
            "fileName": $('#file')[0].files[0].name,
            "fileType": 1,
            "fileBase64": base64data.split(",")[1],
            "avatar": true,
            "x1": selectedArea.getSelection().x1,
            "x2": selectedArea.getSelection().x2,
            "y1": selectedArea.getSelection().y1,
            "y2": selectedArea.getSelection().y2,
            "width": imgWidth,
            "height": imgHeight,
            "scale": imgScale
        });
        $.ajax({
            url: saveFile,
            type: "POST",
            contentType: 'application/json',
            datatype: "json",
            data: message,
            async: false,
            success: function (data, textStatus, jqXHR) {
                uploadAvatarForCurrent(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(textStatus);
            }
        });
    };
}

function saveFileOnServer(callback) {
    var reader = new FileReader();
    reader.readAsDataURL($('#fileMessage')[0].files[0]);
    reader.onloadend = function () {
        var base64data = reader.result;
        var message = JSON.stringify({
            "fileName": $('#fileMessage')[0].files[0].name,
            "fileType": 3,
            "fileBase64": base64data.split(",")[1],
            "avatar": false,
        });
        $.ajax({
            url: saveFile,
            type: "POST",
            contentType: 'application/json',
            datatype: "json",
            data: message,
            async: true,
            success: function (data, textStatus, jqXHR) {
                path = data;
               callback();
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(textStatus);
            }
        });
    };
    
}

function avatarPicked() {
    var reader = new FileReader();
    reader.readAsDataURL($('#file')[0].files[0]);
    reader.onloadend = function () {


        $("#imgToLoad").removeAttr("width");
        $("#imgToLoad").removeAttr("height");
        $("#imgToLoad").attr("src", reader.result);
        $("#imgToLoad").removeAttr("hidden");
        $("#divButtonSaveAvatar").removeAttr("hidden");
        $("#labelSelectArea").removeAttr("hidden");
    };
}

function filePicked() {
    var reader = new FileReader();
    reader.readAsDataURL($('#fileMessage')[0].files[0]);
    reader.onloadend = function () {
        $("#divButtonSend").removeAttr("hidden");
        $("#sendFile").removeAttr("hidden");
    };
}

function initAvatarUpload() {
    $('#changePictureModal').on('hide.bs.modal', function () {
        if (selectedArea != null)
            selectedArea.setOptions({disable: true, hide: true});
    });
    $('#changePictureModal').on('shown.bs.modal', function () {
        if (selectedArea != null)
            selectedArea.setOptions({
                enable: true,
                x1: selectedArea.getSelection().x1,
                y1: selectedArea.getSelection().y1,
                x2: selectedArea.getSelection().x2,
                y2: selectedArea.getSelection().y2
            });
    });
    $('#changePictureModal').on('scroll', function () {
        if (selectedArea != null)
            selectedArea.setOptions({
                enable: true,
                x1: selectedArea.getSelection().x1,
                y1: selectedArea.getSelection().y1,
                x2: selectedArea.getSelection().x2,
                y2: selectedArea.getSelection().y2
            });
    });
    $("#imgToLoad").on("load", function () {
        imgWidth = $("#imgToLoad").width();
        imgHeight = $("#imgToLoad").height();
        if (imgWidth > imgHeight) {
            $("#imgToLoad").attr("width", 500);
            $("#imgToLoad").attr("height", Math.floor(imgHeight / (imgWidth / 500)));
            imgHeight = Math.floor(imgHeight / (imgWidth / 500));
            imgScale = (imgWidth / 500);
            imgWidth = 500;
        } else {
            $("#imgToLoad").attr("height", 500);
            $("#imgToLoad").attr("width", Math.floor(imgWidth / (imgHeight / 500)));
            imgWidth = Math.floor(imgWidth / (imgHeight / 500));
            imgScale = (imgHeight / 500);
            imgHeight = 500;
        }
        selectedArea = $("#imgToLoad").imgAreaSelect({
            x1: 0,
            y1: 0,
            x2: Math.floor(Math.min(imgHeight, imgWidth)),
            y2: Math.floor(Math.min(imgHeight, imgWidth)),
            aspectRatio: "1:1",
            handles: "corners",
            instance: true
        });
    });
}
