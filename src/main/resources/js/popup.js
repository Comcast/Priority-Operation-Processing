function showPopup(event, visible, showCloseButton){
    $("#popup_background").toggle(visible);
    $("#popup_window").toggle(visible);

    var toggleCloseButton = true;
    if(defined(showCloseButton)){
        toggleCloseButton = showCloseButton;
    }
    $("#popup_close_button_container").toggle(toggleCloseButton);
}

function showPopupWithHtml(event, titleHTML, contentHTML, showCloseButton){
    $("#popup_windowheader").html(titleHTML);
    $("#popup_content").html(contentHTML);
    showPopup(event, true, showCloseButton);
}
