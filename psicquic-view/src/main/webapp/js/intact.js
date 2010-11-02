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
    hide('graphController');
    show('interactionResults');

    _gaq.push(['_trackEvent', 'Table', '#{searchBean.selectedServiceName}', '#{searchBean.userQuery.searchQuery}']);
}

function selectGraphTab() {
    document.getElementById('graphLabel').style.fontWeight='bold';
    document.getElementById('tableLabel').style.fontWeight='normal';

    hide('interactionResults');
    show('cytoscapeweb');
    show('graphController');

    _gaq.push(['_trackEvent', 'Graph', '#{searchBean.selectedServiceName}', '#{searchBean.userQuery.searchQuery}']);
}

///////////////////////////////
// Cytoscape Web interactions

function selectMerged() {
    document.getElementById('mergeOn').style.fontWeight='bold';
    document.getElementById('mergeOff').style.fontWeight='normal';

    merged = true;
    vis.edgesMerged( merged );
}

function unselectMerged() {
    document.getElementById('mergeOn').style.fontWeight='normal';
    document.getElementById('mergeOff').style.fontWeight='bold';

    merged = false;
    vis.edgesMerged( merged );
}

function selectForceDirectedLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='bold';
    document.getElementById('radialLayout').style.fontWeight='normal';
    document.getElementById('circleLayout').style.fontWeight='normal';
    vis.layout('ForceDirected');
}

function selectRadialLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('radialLayout').style.fontWeight='bold';
    document.getElementById('circleLayout').style.fontWeight='normal';
    vis.layout('Radial');
}

function selectCircleLayout() {
    document.getElementById('forceDirectedLayout').style.fontWeight='normal';
    document.getElementById('radialLayout').style.fontWeight='normal';
    document.getElementById('circleLayout').style.fontWeight='bold';
    vis.layout('Circle');
}

function graphResize() {
    var heigth = $(window).height() - 290;
    var x = document.getElementById('cytoscapeweb');
    x.style.height = heigth + 'px';
}
