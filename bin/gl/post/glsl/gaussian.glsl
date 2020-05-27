#version 150

in vec2 blurTextureCoords[11];

out vec4 out_color;

uniform sampler2D sampler;

void main(void){
	out_color = vec4(0.0);
	out_color += texture(sampler, blurTextureCoords[0]) * 0.0093;
	out_color += texture(sampler, blurTextureCoords[1]) * 0.028002;
	out_color += texture(sampler, blurTextureCoords[2]) * 0.065984;
	out_color += texture(sampler, blurTextureCoords[3]) * 0.121703;
	out_color += texture(sampler, blurTextureCoords[4]) * 0.175713;
	out_color += texture(sampler, blurTextureCoords[5]) * 0.198596;
	out_color += texture(sampler, blurTextureCoords[6]) * 0.175713;
	out_color += texture(sampler, blurTextureCoords[7]) * 0.121703;
	out_color += texture(sampler, blurTextureCoords[8]) * 0.065984;
	out_color += texture(sampler, blurTextureCoords[9]) * 0.028002;
	out_color += texture(sampler, blurTextureCoords[10]) * 0.0093;
}

	
