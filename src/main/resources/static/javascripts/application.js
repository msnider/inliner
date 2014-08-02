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

function submitFor(exceptFields, urlDataName) {
	var fields = ['#styleTagUrl', '#styles', '#styleAttributeUrl', '#attribute',
          '#linkUrl', '#mediaQuery', '#contents', '#htmlFragmentUrl', '#htmlUrl'
      ];
	return function() {
		for(var i = 0; i < fields.length; i++) {
			var field = fields[i];
			if (exceptFields.indexOf(field) == -1) {
				$(field).prop('disabled', 'disabled');
				console.log('disabling ' + field);
			}
		}
		var $form = $(this).closest('form');console.log($form.data(urlDataName + '-url'));
			$form.prop('action', $form.data(urlDataName + '-url'));
			$form.submit();
		setTimeout(function() {
			for(var i = 0; i < fields.length; i++) {
				var field = fields[i];
				if (exceptFields.indexOf(field) == -1) {
					$(field).removeProp('disabled');
				}
			}
		}, 100);
	};
}

$(document).ready(function() {
	$('#ua').val(navigator.userAgent);
	$('#width').val(window.innerWidth);
	$('#height').val(window.innerHeight);
	$('#deviceWidth').val(screen.width);
	$('#deviceHeight').val(screen.height);
	$('#devicePixelRatio').val(window.devicePixelRatio);
	$('#defaultFontSizePx').val(getDefaultFontSize()[1]);
	
	$('#getLinkTagStyles').click(submitFor(['#linkUrl', '#mediaQuery'], 'styles'));
	$('#getStyleTagStyles').click(submitFor(['#styleTagUrl', '#styles'], 'styles'));
	$('#getStyleAttributeStyles').click(submitFor(['#styleAttributeUrl', '#attribute'], 'styles'));
	$('#getHtml').click(submitFor(['#htmlUrl'], 'page'));
	$('#getHtmlFragment').click(submitFor(['#htmlFragmentUrl', '#contents'], 'page'));
});