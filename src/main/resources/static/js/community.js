/**
 * Created by codedrinker on 2019/6/1.
 */

/**
 * 提交回复
 */
function post() {
    var questionId = $("#question_id").val();//使用jquery的id选择器拿到question_id
    var content = $("#comment_content").val();//回复文本框中填入的值
    comment2target(questionId, 1, content);//通过ajax请求发送给服务端
}

function comment2target(targetId, type, content) {
    if (!content) {
        alert("不能回复空内容~~~");
        return;
    }

    $.ajax({
        type: "POST",
        url: "/comment",//加/表示是请求根目录下的comment
        contentType: 'application/json',//告诉服务器期望的响应对象应该是json类型
        data: JSON.stringify({//data使用stringify方法将js对象转为字符串，发给服务器
            "parentId": targetId,
            "content": content,
            "type": type
        }),
        success: function (response) {//response就是服务器返回的ResultDTO对象，有code和message属性
            if (response.code == 200) {
                window.location.reload();//回复成功，刷新页面
            } else {
                if (response.code == 2003) {//需要登录，跳转登录
                    var isAccepted = confirm(response.message);
                    if (isAccepted) {//说明点了"yes"
                        // $('#myModal').modal({});
                        // window.open("https://github.com/login/oauth/authorize?client_id=7f316909bf70d1eaa2b2&redirect_uri=" + document.location.origin + "/callback&scope=user&state=1");
                        window.open("/callback");
                        window.localStorage.setItem("closable", true);
                        window.location.reload();//回复成功，刷新页面
                    }
                } else {
                    alert(response.message);//回复失败，直接弹出message中的信息
                }
            }
        },
        dataType: "json"
    });
}
//点击展开后的二级回复下面的"评论"按钮，执行下面代码
function comment(e) {
    var commentId = e.getAttribute("data-id");
    var content = $("#input-" + commentId).val();//评论文本框中的内容
    comment2target(commentId, 2, content);//通过ajax请求发送给服务端
}

/**
 * 展开二级评论()
 *   这里相当于手写拼出了显示所有二级评论的内容前端
 */
function collapseComments(e) {
    var id = e.getAttribute("data-id");
    var comments = $("#comment-" + id);

    // 获取一下二级评论的展开状态
    var collapse = e.getAttribute("data-collapse");
    if (collapse) {
        // 折叠二级评论
        comments.removeClass("in");//去掉in消失
        e.removeAttribute("data-collapse");
        e.classList.remove("active");//展开变蓝，折叠回灰色
    } else {
        var subCommentContainer = $("#comment-" + id);
        if (subCommentContainer.children().length != 1) {
            //展开二级评论
            comments.addClass("in");
            // 标记二级评论展开状态
            e.setAttribute("data-collapse", "in");
            e.classList.add("active");
        } else {
            $.getJSON("/comment/" + id, function (data) {
                $.each(data.data.reverse(), function (index, comment) {
                    var mediaLeftElement = $("<div/>", {
                        "class": "media-left"
                    }).append($("<img/>", {
                        "class": "media-object img-rounded",
                        "src": comment.user.avatarUrl
                    }));

                    var mediaBodyElement = $("<div/>", {
                        "class": "media-body"
                    }).append($("<h5/>", {
                        "class": "media-heading",
                        "html": comment.user.name
                    })).append($("<div/>", {
                        "html": comment.content
                    })).append($("<div/>", {
                        "class": "menu"
                    }).append($("<span/>", {
                        "class": "pull-right",
                        "html": moment(comment.gmtCreate).format('YYYY-MM-DD')
                    })));

                    var mediaElement = $("<div/>", {
                        "class": "media"
                    }).append(mediaLeftElement).append(mediaBodyElement);

                    var commentElement = $("<div/>", {
                        "class": "col-lg-12 col-md-12 col-sm-12 col-xs-12 comments"
                    }).append(mediaElement);

                    subCommentContainer.prepend(commentElement);
                });
                //展开二级评论
                comments.addClass("in");
                // 标记二级评论展开状态
                e.setAttribute("data-collapse", "in");
                e.classList.add("active");
            });
        }
    }
}
//点击标签输入框，显示标签页面
function showSelectTag() {
    $("#select-tag").show();
    alert("为什么不显示呢");
}
//通过点选标签页面中的标签，会往标签框中输入对应内容
function selectTag(e) {
    var value = e.getAttribute("data-tag");
    var previous = $("#tag").val();

    if (previous) {
        var index = 0;
        var appear = false; //记录value是否已经作为一个独立的标签出现过
        while (true) {
            index = previous.indexOf(value, index); //value字符串在previous中出现的位置
            if (index == -1) break;
            //判断previous中出现的value是否是另一个标签的一部分
            //即value的前一个和后一个字符都是逗号","或者没有字符时，才说明value是一个独立的标签
            if ((index == 0 || previous.charAt(index - 1) == ",")
                && (index + value.length == previous.length || previous.charAt(index + value.length) == ",")
               ) {
                appear = true;
                break;
            }
            index++; //用于搜索下一个出现位置
        }
        if (!appear) {
            //若value没有作为一个独立的标签出现过
            $("#tag").val(previous + ',' + value);
        }
    }
    else {
        $("#tag").val(value);
    }
}