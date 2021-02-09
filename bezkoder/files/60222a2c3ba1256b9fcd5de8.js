$(document).ready(function(){
    var origin_main_contents_style = $(".main_contents").css("padding-top");
    var current_page = window.location.pathname;
    
    // 메뉴 실행
    $(".all").on("click", function(){
        $(".nav").addClass("on");

        // index 페이지만 메뉴 실행시 padding 0 => 이미지 위 여백 드러남
        if ($(current_page.split("/")).get(-1) == "index.html"){
            $(".main_contents").css('padding-top','0px');
        }
        $("header").css('background-color','transparent') 
        $("header h1 a").css('display','none') 
        $(".dim").show();
    })
    // 메뉴 끄기
    $(".nav .close").on("click", function(){
        $(".nav").removeClass("on");
        if ($(current_page.split("/")).get(-1) == "index.html"){
            $(".main_contents").css('padding-top',origin_main_contents_style);
        }
        $(".dim").hide();
        $("header").css('background-color','white') 
        $("header h1 a").css('display','block') 
        $(".main_contents").css('padding-top',origin_main_contents_style);
    })
});