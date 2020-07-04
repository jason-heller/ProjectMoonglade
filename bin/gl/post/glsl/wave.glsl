#version 150

in vec2 pass_textureCoords;
uniform sampler2D sampler;
uniform vec3 color;
uniform float timer;

out vec4 out_color;

vec2 Wave(vec2 p) {
	float pi = 3.1415;
	float amplitude = 0.002;
	float freq = 40.0 * pi;
	float y = sin(freq*p.x + (timer*4.0)) * amplitude;
	float x = cos(freq*p.y + (timer*4.0)) * amplitude;
	return vec2(p.x+x, p.y+y);
}

void main(void){
	vec2 uv = Wave( pass_textureCoords ); 
    out_color = mix(vec4(color.xyz, 1.0), texture(sampler, vec2(uv.x, uv.y)), 0.75);
    //out_color.b = (out_color.r * .393) + (out_color.g *.769) + (out_color.b * .393);
	//out_color.g = (out_color.r * .349) + (out_color.g *.686) + (out_color.b * .168);
	//out_color.r = (out_color.r * .272) + (out_color.g *.534) + (out_color.b * .131);
}