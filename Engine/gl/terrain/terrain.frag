#version 150

in vec4 pass_uvs;
in vec3 pass_normals;
in vec4 pass_colors;

uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform sampler2D sampler3;
uniform sampler2D sampler4;

uniform vec3 lightDirection;

out vec4 out_color;

const float lightDiffuse = 0.5;
const float lightScale = 0.3;

void main(void) {
	float ambientLight = lightDiffuse + (lightDirection.y*0.2);

	float light = (dot(vec3(lightDirection.x, 0, lightDirection.z), pass_normals)*lightScale) + ambientLight;
	
	if (pass_colors.a == 1.0) {
		vec4 t1 = mix(texture(sampler1, pass_uvs.xy), texture(sampler3, pass_uvs.xy), pass_uvs.z);
		vec4 t2 = mix(texture(sampler2, pass_uvs.xy), texture(sampler4, pass_uvs.xy), pass_uvs.w);
		
		out_color = (t1+t2)/2.0 * pass_colors * light;
	} else {
		vec4 tex = texture(sampler1, pass_uvs.xy);
		if (tex.a < 0.5) discard;
		out_color = tex * light;
	}
}
