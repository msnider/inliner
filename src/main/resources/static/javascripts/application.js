function getDefaultFontSize(pa) {
	pa = pa || document.body;
	var who = document.createElement('div');
	who.style.cssText = 'display:inline-block; padding:0; line-height:1; position:absolute; visibility:hidden; font-size:1em';
	who.appendChild(document.createTextNode('M'));
	pa.appendChild(who);
	var fs = [who.offsetWidth, who.offsetHeight];
	pa.removeChild(who);
	return fs;
}

$(document).ready(function() {
	$('#ua').val(navigator.userAgent);
	$('#width').val(window.innerWidth);
	$('#height').val(window.innerHeight);
	$('#deviceWidth').val(screen.width);
	$('#deviceHeight').val(screen.height);
	$('#devicePixelRatio').val(window.devicePixelRatio);
	$('#defaultFontSizePx').val(getDefaultFontSize()[1]);
	
	$('form.form-inliner').submit(function(e) {
		e.preventDefault();
		$(this).find('input[type=hidden]').remove();
		$(this).append(
			$('<input type="hidden" name="ua">').val($('#ua').val()),
			$('<input type="hidden" name="width">').val($('#width').val()),
			$('<input type="hidden" name="height">').val($('#height').val()),
			$('<input type="hidden" name="deviceWidth">').val($('#deviceWidth').val()),
			$('<input type="hidden" name="deviceHeight">').val($('#deviceHeight').val()),
			$('<input type="hidden" name="devicePixelRatio">').val($('#devicePixelRatio').val()),
			$('<input type="hidden" name="defaultFontSizePx">').val($('#defaultFontSizePx').val())
		);
		$(this).get(0).submit();
	});
});