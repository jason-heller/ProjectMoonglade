#version 150

in vec2 in_position;
in vec2 in_textureCoords;

out vec2 blurTextureCoords[11];

const float height = 720.0/8.0;

void main(void){

	gl_Position = vec4(in_position*2.0, 0.0, 1.0);
	vec2 centerTexCoords = in_position + 0.5;
	float pixelSize = 1.0 / height;
	
	for(int i = -5; i <= 5; i++) {
		blurTextureCoords[i+5] = centerTexCoords + vec2(0.0, pixelSize * i);
	}
}
