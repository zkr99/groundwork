class Groundwork < Formula
  desc "CLI for bootstrapping new projects from recurring local code patterns"
  homepage "https://github.com/zkr99/groundwork"
  url "https://github.com/zkr99/groundwork/releases/download/v0.1.0-alpha.2/groundwork-0.1.0-alpha.2.tar"
  sha256 "cca59b73db0f91df490cae4bacf626c9da8ebc1d231f1dd2372612813e647be6"
  license "MIT"

  depends_on "openjdk"

  def install
    libexec.install Dir["*"]

    Pathname.glob("#{libexec}/bin/*") do |file|
      next if file.directory?
      next if file.extname == ".bat"

      (bin/file.basename).write_env_script file, Language::Java.overridable_java_home_env
    end
  end

  test do
    assert_match "Groundwork 0.1.0-alpha.2", shell_output("#{bin}/groundwork --version")
  end
end
