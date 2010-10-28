function submitEnter(commandId, e)
{
    var keycode;
    if (window.event)
        keycode = window.event.keyCode;
    else if (e)
        keycode = e.which;
    else
        return true;

    if (keycode == 13) {
        document.getElementById(commandId).click();
        return false;
    } else
        return true;

}

function showhide(id){
    if (document.getElementById){
        obj = document.getElementById(id);
        if (obj.style.display == "none"){
            obj.style.display = "";
        } else {
            obj.style.display = "none";
        }
    }
}

function show(id){
    if (document.getElementById){
        obj = document.getElementById(id);
        if (obj.style.display == "none"){
            obj.style.display = "block";
        }
    }
}

function hide(id){
    if (document.getElementById){
        obj = document.getElementById(id);
        if (obj.style.display == "block" || obj.style.display == ""){
            obj.style.display = "none";
        }
    }
}

function selectTableTab() {
    document.getElementById('graphLabel').style.fontWeight='normal';
    document.getElementById('tableLabel').style.fontWeight='bold';
    hide('cytoscapeweb');
    show('interactionResults');
    _gaq.push(['_trackEvent', 'Table', '#{searchBean.selectedServiceName}', '#{searchBean.userQuery.searchQuery}']);
}

function selectGraphTab() {
    document.getElementById('graphLabel').style.fontWeight='bold';
    document.getElementById('tableLabel').style.fontWeight='normal';
    hide('interactionResults');
    show('cytoscapeweb');
    _gaq.push(['_trackEvent', 'Graph', '#{searchBean.selectedServiceName}', '#{searchBean.userQuery.searchQuery}']);
}