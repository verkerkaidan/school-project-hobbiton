defmodule MediaServiceElxWeb.MediaFilesJSON do
  alias MediaServiceElx.MediaFiles.MediaFile
  alias MediaServiceElx.MediaInstances.MediaInstance

  def instance_index(%{instances: instances}) do
    %{data: for(instance <- instances, do: instance_data(instance))}
  end

  def file_show(%{instance: instance}) do
    %{data: instance_data(instance)}
  end

  defp instance_data(%MediaInstance{} = instance) do
    %{
      id: instance.id
    }
  end

  def file_index(%{files: files}) do
    %{data: for(file <- files, do: file_data(file))}
  end

  def file_show(%{file: file}) do
    %{data: file_data(file)}
  end

  defp file_data(%MediaFile{} = file) do
    %{
      id: file.id,
      content_flag: file.content_flag,
      original: file.original,
      content_type: file.content_type
    }
  end
end