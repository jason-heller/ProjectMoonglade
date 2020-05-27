#version 150

in vec2 pass_textureCoords;

out vec4 out_color;

uniform sampler2D sampler;

void main(void) {
	vec4 color = texture(sampler, pass_textureCoords);
	float brightness = (color.r * 0.2126) + (color.g * 0.7152) + (color.b * 0.0722);
	out_color = color * brightness;
}

	
