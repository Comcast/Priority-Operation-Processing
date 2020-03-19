function showPopup(event, visible, showCloseButton){
    $("#popup_background").toggle(visible);
    $("#popup_window").toggle(visible);

    var toggleCloseButton = true;
    if(defined(showCloseButton)){
        toggleCloseButton = showCloseButton;
    }
    $("#popup_close_button_container").toggle(toggleCloseButton);
}

function showPopupWithHtml(event, html, showCloseButton){
    $("#popup_content").html(html);
    showPopup(event, true, showCloseButton);
}

// TODO: either expand showPopupWithHtml with params for size/location stuff or make another method