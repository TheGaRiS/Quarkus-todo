function refresh() {
    $.get('/task', function (tasks) {
        var list = '';
        (tasks || []).forEach(function (task) {
            list = list
                + '<tr>'
                + '<td>' + task.id + '</td>'
                + '<td>' + task.name + '</td>'
                + '<td>' + task.state + '</td>'
                + '<td><a href="#" onclick="updateTask(' + task.id + ')">Done</a></td>'
                + '<td><a href="#" onclick="deleteTask(' + task.id + ')">Delete</a></td>'
                + '</tr>'
        });
        if (list.length > 0) {
            list = ''
                + '<table><thead><th>Id</th><th>Name</th><th>State</th></thead>'
                + list
                + '</table>';
        } else {
            list = "No tasks in database"
        }
        $('#all-tasks').html(list);
    });
}

function deleteTask(id) {
    $.ajax('/task/' + id, {method: 'DELETE'}).then(refresh);
}

function updateTask(id) {
    $.ajax('/task/' + id, {method: 'PUT'}).then(refresh);
}

$(document).ready(function () {

    $('#create-task-button').click(function () {
        var taskName = $('#task-name').val();
        $.post({
            url: '/task',
            contentType: 'application/json',
            data: JSON.stringify({name: taskName})
        }).then(refresh);
    });

    refresh();
});