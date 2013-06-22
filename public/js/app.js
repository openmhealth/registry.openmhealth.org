var app = {
  init: function() {
  },
  syntaxHigh: function(res) {
    json = JSON.stringify(res, undefined, 2);
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
      var cls = 'number';
      if (/^"/.test(match)) {
        if (/:$/.test(match)) {
          cls = 'key';
        } else {
          cls = 'string';
        }
      } else if (/true|false/.test(match)) {
        cls = 'boolean';
      } else if (/null/.test(match)) {
        cls = 'null';
      }
      return '<span class="' + cls + '">' + match + '</span>';
    });
  }, openIdInit: function() {
    $("#openIdURL").change(function() {
      var select = $(this);
      var val = select.find("option:selected").val();
      if (val === "other") {
        $("#otherID").show();
      } else {
        $("#otherID").hide();
      }
    });
  },
  hasPasswordInit: function() {
    $("form.hasPassword").submit(function() {
      var form = $(this);
      var fakePassword = form.find("[name=fakePassword]");
      form.find("#password").val(fakePassword.val());
      return true;
    });
  },
  hasPasswordRepeatInit: function() {
    $("form.hasPasswordRepeat").submit(function() {
      var form = $(this);
      var fakePassword = form.find("[name=fakePassword]");
      var fakePasswordRepeat = form.find("[name=fakePasswordRepeat]");
      if (fakePassword.val() !== fakePasswordRepeat.val()) {
        $(".alert .msg").text("The passwords do not match.");
        $(".alert").removeClass("hidden");
        $(window).scrollTop(0);
        return false;
      } else {
        form.find("#password").val(fakePassword.val());
        return true;
      }
    });
  }
};
app.init();