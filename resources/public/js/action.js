/***
* ajax crafting using jquery - with CSRF token
***/
var csrf_token = document.getElementById("__anti-forgery-token").value;

$( ".import_job" ).click(sendJob);
$( ".bulk-action" ).click({action: ""}, sendBulkAction);
$( ".accept" ).click({action: "accept"}, sendAction);
$( ".reject" ).click({action: "reject"}, sendAction);
$( ".resolve" ).click({action: "resolve"}, sendAction);
$( ".unaccept" ).click({action: "unaccept"}, sendAction);
$( ".comment" ).click(showComment);
$( ".comment_button" ).click(sendComment);
$( "#cancelLDAP" ).click(hideLDAPForm);
$( "#runLDAP" ).click(runLDAPJob);
$( ".comment_val" ).bind('keydown', function(event) {
  if(event.which == 13) {
    $(this).parent().parent().find(".comment_button").click();
  }
});

function sendAction(event) {
  $.ajax({
    type: "POST",
    url: context + "/entry-action",
    beforeSend: function(xhr) {
      xhr.setRequestHeader('X-CSRF-Token', csrf_token);
    },
    data: {
      app_id: $(".app_id", $(this).parent()).html(),
      id: $(".id", $(this).parent()).html(),
      action: event.data.action
    }
  })
  .done(function(returned) {
    if (returned !== "0")
      alert(returned);
  });
  $(this).parent().fadeOut();
  return false;
}

function sendBulkAction(event) {
    alert("Job submitted");
    $.ajax({
      type: "POST",
      url: context + "/entry-action",
      beforeSend: function(xhr) {
        xhr.setRequestHeader('X-CSRF-Token', csrf_token);
      },
      data: {
        app_id: $( "#app-selector" ).val(),
        action: $( ".bulk-action" ).attr('id'),
        id: $( ".bulk-action" ).attr('value')
      }
    })
    .done(function(returned) {
      if (returned !== "0")
        alert(returned);
    });
    return false;
}

function sendJob(event) {
  if ($(this).attr('href') == 2) {
    $( "#ldap-auth-form" ).css( "display", "table-cell");
    $(this).fadeOut("fast");
    return false;
  } else {
    $.ajax({
      type: "POST",
      url: context + "/jobs",
      beforeSend: function(xhr) {
        xhr.setRequestHeader('X-CSRF-Token', csrf_token);
      },
      data: {
        app_id: $(this).attr('href')
      }
    })
    .done(function(returned) {
      if (returned !== "0")
        alert(returned);
    });
  }
  $(this).fadeOut("fast");
  return false;
}

function runLDAPJob() {
  $.ajax({
    type: "POST",
    url: context + "/jobs",
    beforeSend: function(xhr) {
      xhr.setRequestHeader('X-CSRF-Token', csrf_token);
    },
    data: {
      app_id: $("#app_id").val(),
      username: $("#username").val(),
      password: $("#password").val()
    }
  })
    .done(function(returned) {
      if (returned !== "0")
        alert(returned);
    });
  $( "#ldap-auth-form" ).css( "display", "none");
  return false;
}

function hideLDAPForm() {
  $( "#ldap-auth-form" ).css( "display", "none");
  return false;
}

function showComment() {
  $(this).parent().next().find(".comment_val").slideToggle("fast");
  $(this).parent().next().find(".comment_button").slideToggle("fast");
  return false;
}

function sendComment(event) {
    var textbox = $(this).parent().parent().find(".comment_val");
    $.ajax({
    type: "POST",
    url: context + "/entry-comment",
    beforeSend: function(xhr) {
      xhr.setRequestHeader('X-CSRF-Token', csrf_token);
    },
    data: {
      app_id: textbox.attr("name"),
      entry_id: textbox.attr("id"),
      comments: textbox.val()
    }
  })
  .done(function(returned) {
    if (returned !== "0")
      alert(returned);
  });
  textbox.fadeOut("fast");
  $(this).fadeOut("fast");
}
