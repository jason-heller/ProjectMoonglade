#version 150

in vec3 pass_uvs;
uniform samplerCube sampler;
out vec4 out_color;
in float pass_bgAlpha;

uniform vec3 topColor;
uniform vec3 bottomColor;
uniform vec3 weatherColor;

void main(void){
	if (weatherColor.x == 10.0) {
		out_color = vec4(.9,.9,.85,1);
	} else {
		vec4 finalColor = texture(sampler, pass_uvs);
		vec4 bgColor = vec4(mix(topColor, bottomColor, -pass_uvs.y/100.0),1.0);
		
		if (finalColor.a == 1.0) {
			out_color = mix(bgColor, finalColor, pass_bgAlpha*((weatherColor.x-0.5)*2.0));	
		} else {
			out_color = bgColor;	
		}
		
		out_color = mix(out_color, vec4(weatherColor.xyz, 1.0), 1.0-weatherColor.x);
		
	}
}
